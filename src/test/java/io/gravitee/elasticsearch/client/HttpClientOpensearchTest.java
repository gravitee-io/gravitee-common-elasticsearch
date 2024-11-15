/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.elasticsearch.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.gravitee.elasticsearch.client.http.HttpClient;
import io.gravitee.elasticsearch.client.http.HttpClientConfiguration;
import io.gravitee.elasticsearch.config.Endpoint;
import io.gravitee.elasticsearch.model.Health;
import io.gravitee.elasticsearch.version.ElasticsearchInfo;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.rxjava3.core.Vertx;
import java.time.Duration;
import java.util.Collections;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author GraviteeSource Team
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HttpClientOpensearchTest.TestConfig.class })
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class HttpClientOpensearchTest {

    private static final String OPENSEARCH_DEFAULT_VERSION = "2.11.0";
    private static final String CLUSTER_NAME = "gravitee_test";
    private static final String TEMPLATE =
        """
        {
          "index_patterns": ["te*", "bar*"],
          "template": {
            "settings": {
              "number_of_shards": 1
            },
            "mappings": {
              "_source": {
                "enabled": true
              },
              "properties": {
                "host_name": {
                  "type": "keyword"
                },
                "created_at": {
                  "type": "date",
                  "format": "EEE MMM dd HH:mm:ss Z yyyy"
                }
              }
            },
            "aliases": {
              "mydata": { }
            }
          },
          "priority": 500
        }
    """;
    private static final String POLICY =
        """
        {
          "policy": {
            "policy_id": "%s",
            "description": "Policy to manage rollover for %s indices",
            "default_state": "rollover_state",
            "states": [
              {
                "name": "rollover_state",
                "actions": [
                  {
                    "rollover": {
                      "min_size": "50gb",
                      "min_index_age": "7d"
                    }
                  }
                ],
                "transitions": []
              }
            ],
            "ism_template": {
              "index_patterns": ["%s"]
            }
          }
        }
    """;

    @Autowired
    private Client client;

    @Test
    @SneakyThrows
    public void should_get_version() {
        Single<ElasticsearchInfo> info = client.getInfo();

        TestObserver<ElasticsearchInfo> observer = info.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        String esVersion = System.getProperty("elasticsearch.version", OPENSEARCH_DEFAULT_VERSION);
        observer.assertValue(elasticsearchInfo -> esVersion.equals(elasticsearchInfo.getVersion().getNumber()));
    }

    @Test
    @SneakyThrows
    public void should_get_health() {
        Single<Health> health = client.getClusterHealth();

        TestObserver<Health> observer = health.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertValue(health1 -> CLUSTER_NAME.equals(health1.getClusterName()));
    }

    @Test
    @SneakyThrows
    public void should_get_alias() {
        String template = "{\"aliases\":{\"gravitee_test_alias\":{\"is_write_index\": true}}}";
        String expectedAlias = "{\"gravitee_test\":{\"aliases\":{\"gravitee_test_alias\":{\"is_write_index\":true}}}}";

        Maybe<JsonNode> alias = client.createIndexWithAlias("gravitee_test", template).andThen(client.getAlias("gravitee_test_alias"));

        TestObserver<JsonNode> observer = alias.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertValue(node -> expectedAlias.equals(node.toString()));
    }

    @Test
    @SneakyThrows
    public void should_put_index_template() {
        client.putIndexTemplate("gravitee_test_index_template", TEMPLATE).test().await().assertNoErrors().assertComplete();
    }

    @Test
    @SneakyThrows
    public void should_not_get_alias() {
        Maybe<JsonNode> alias = client.getAlias("gravitee_test_alias");

        TestObserver<JsonNode> observer = alias.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertNoValues();
    }

    @Configuration
    public static class TestConfig {

        @Value("${opensearch.version:" + OPENSEARCH_DEFAULT_VERSION + "}")
        private String opensearchVersion;

        @Bean
        public Vertx vertx() {
            return Vertx.vertx();
        }

        @Bean
        public Client client(HttpClientConfiguration clientConfiguration) {
            return new HttpClient(clientConfiguration);
        }

        @Bean
        public HttpClientConfiguration configuration(OpensearchContainer<?> opensearchContainer) {
            HttpClientConfiguration opensearchConfiguration = new HttpClientConfiguration();
            opensearchConfiguration.setEndpoints(
                Collections.singletonList(
                    new Endpoint("http://" + opensearchContainer.getHost() + ":" + opensearchContainer.getMappedPort(9200))
                )
            );
            return opensearchConfiguration;
        }

        @Bean(destroyMethod = "close")
        public OpensearchContainer<?> opensearchContainer() {
            final OpensearchContainer<?> opensearchContainer = new OpensearchContainer<>(
                "opensearchproject/opensearch:" + opensearchVersion
            );
            opensearchContainer
                .withEnv("cluster.name", CLUSTER_NAME)
                .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                .withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
                .waitingFor(Wait.forHttp("/_cluster/health").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));
            opensearchContainer.start();
            return opensearchContainer;
        }
    }
}
