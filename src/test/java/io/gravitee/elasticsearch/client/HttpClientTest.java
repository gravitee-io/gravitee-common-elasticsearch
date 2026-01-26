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
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HttpClientTest.TestConfig.class })
public class HttpClientTest {

    public static final String ELASTICSEARCH_DEFAULT_VERSION = "8.5.2";

    public static final String CLUSTER_NAME = "gravitee_test";

    /**
     * Elasticsearch client.
     */
    @Autowired
    private Client client;

    @Test
    public void shouldGetVersion() throws InterruptedException {
        Single<ElasticsearchInfo> info = client.getInfo();

        TestObserver<ElasticsearchInfo> observer = info.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        String esVersion = System.getProperty("elasticsearch.version", ELASTICSEARCH_DEFAULT_VERSION);
        observer.assertValue(elasticsearchInfo -> esVersion.equals(elasticsearchInfo.getVersion().getNumber()));
    }

    @Test
    public void shouldGetHealth() throws InterruptedException {
        Single<Health> health = client.getClusterHealth();

        TestObserver<Health> observer = health.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertValue(health1 -> CLUSTER_NAME.equals(health1.getClusterName()));
    }

    @Test
    public void shouldGetAlias() throws InterruptedException {
        String template = "{\"aliases\":{\"gravitee_test_alias\":{\"is_write_index\": true}}}";
        String esVersion = System.getProperty("elasticsearch.version", ELASTICSEARCH_DEFAULT_VERSION);
        String expectedAlias = esVersion.startsWith("5")
            ? "{\"gravitee_test\":{\"aliases\":{\"gravitee_test_alias\":{}}}}"
            : "{\"gravitee_test\":{\"aliases\":{\"gravitee_test_alias\":{\"is_write_index\":true}}}}";

        Maybe<JsonNode> alias = client.createIndexWithAlias("gravitee_test", template).andThen(client.getAlias("gravitee_test_alias"));

        TestObserver<JsonNode> observer = alias.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertValue(node -> expectedAlias.equals(node.toString()));
    }

    @Test
    public void shouldPutIndexTemplate() throws InterruptedException {
        String template = """
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

        client.putIndexTemplate("gravitee_test_index_template", template).test().await().assertNoErrors().assertComplete();
    }

    @Test
    public void shouldNotGetAlias() throws InterruptedException {
        Maybe<JsonNode> alias = client.getAlias("gravitee_test_alias");

        TestObserver<JsonNode> observer = alias.test();
        observer.await();

        observer.assertNoErrors();
        observer.assertComplete();
        observer.assertNoValues();
    }

    @Test
    public void shouldGetFieldTypes() throws InterruptedException {
        String template = """
            {
              "mappings": {
                "properties": {
                  "api-id": {
                    "type": "keyword"
                  }
                }
              },
              "aliases": {}
            }
            """;
        client.createIndexWithAlias("gravitee_field_types_test_1", template).test().await().assertNoErrors().assertComplete();
        client.createIndexWithAlias("gravitee_field_types_test_2", template).test().await().assertNoErrors().assertComplete();

        client
            .getFieldTypes("gravitee_field_types_test_*", "api-id")
            .test()
            .await()
            .assertNoErrors()
            .assertValue(fieldTypes -> fieldTypes.size() == 2 && fieldTypes.stream().allMatch("keyword"::equals));
    }

    @Configuration
    public static class TestConfig {

        @Value("${elasticsearch.version:" + ELASTICSEARCH_DEFAULT_VERSION + "}")
        private String elasticsearchVersion;

        @Bean
        public Vertx vertx() {
            return Vertx.vertx();
        }

        @Bean
        public Client client(HttpClientConfiguration clientConfiguration) {
            return new HttpClient(clientConfiguration);
        }

        @Bean
        public HttpClientConfiguration configuration(ElasticsearchContainer elasticSearchContainer) {
            HttpClientConfiguration elasticConfiguration = new HttpClientConfiguration();
            elasticConfiguration.setEndpoints(
                Collections.singletonList(new Endpoint("http://" + elasticSearchContainer.getHttpHostAddress()))
            );
            elasticConfiguration.setUsername("elastic");
            elasticConfiguration.setPassword(ElasticsearchContainer.ELASTICSEARCH_DEFAULT_PASSWORD);
            return elasticConfiguration;
        }

        @Bean(destroyMethod = "close")
        public ElasticsearchContainer elasticSearchContainer() {
            final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
                "docker.elastic.co/elasticsearch/elasticsearch:" + elasticsearchVersion
            );
            elasticsearchContainer.withEnv("cluster.name", CLUSTER_NAME);
            if (elasticsearchVersion.startsWith("8")) {
                elasticsearchContainer.withEnv("xpack.security.enabled", "false");
            }
            elasticsearchContainer.start();
            return elasticsearchContainer;
        }
    }
}
