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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import io.gravitee.elasticsearch.client.http.HttpClient;
import io.gravitee.elasticsearch.client.http.HttpClientConfiguration;
import io.gravitee.elasticsearch.config.Endpoint;
import io.gravitee.elasticsearch.exception.ElasticsearchException;
import io.vertx.core.buffer.Buffer;
import io.vertx.rxjava3.core.Vertx;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * Index templates and data stream lifecycles, which need Elasticsearch 8.14 or later.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HttpClientDataStreamLifecycleTest.TestConfig.class })
class HttpClientDataStreamLifecycleTest {

    private static final String ELASTICSEARCH_VERSION = "8.17.2";
    private static final String TEMPLATE = """
        {
          "index_patterns": ["%s"],
          "data_stream": {},
          "priority": 500,
          "template": { "lifecycle": { "data_retention": "45d" } },
          "_meta": { "managed_by": "gravitee-apim", "apim_version": "4.14.0" }
        }
        """;

    @Autowired
    private Client client;

    @Test
    void should_get_nothing_for_an_index_template_that_does_not_exist() throws InterruptedException {
        client.getIndexTemplate("gravitee-missing-template").test().await().assertNoErrors().assertComplete().assertNoValues();
    }

    @Test
    void should_get_the_definition_of_an_index_template() throws InterruptedException {
        client.putIndexTemplate("gravitee-template", TEMPLATE.formatted("gravitee-template")).test().await().assertComplete();

        final JsonNode template = client.getIndexTemplate("gravitee-template").test().await().assertComplete().values().get(0);

        assertThat(template.at("/_meta/managed_by").asText()).isEqualTo("gravitee-apim");
        assertThat(template.at("/template/lifecycle/data_retention").asText()).isEqualTo("45d");
        assertThat(template.at("/index_patterns/0").asText()).isEqualTo("gravitee-template");
    }

    @Test
    void should_get_nothing_for_the_lifecycle_of_a_data_stream_that_does_not_exist() throws InterruptedException {
        client.getDataStreamLifecycle("gravitee-missing-stream").test().await().assertNoErrors().assertComplete().assertNoValues();
    }

    @Test
    void should_get_then_change_the_lifecycle_of_a_data_stream() throws InterruptedException {
        createDataStream("gravitee-stream");

        final JsonNode lifecycle = client.getDataStreamLifecycle("gravitee-stream").test().await().assertComplete().values().get(0);
        assertThat(lifecycle.path("data_retention").asText()).isEqualTo("45d");

        client.putDataStreamLifecycle("gravitee-stream", "{\"data_retention\":\"90d\"}").test().await().assertNoErrors().assertComplete();

        final JsonNode changed = client.getDataStreamLifecycle("gravitee-stream").test().await().assertComplete().values().get(0);
        assertThat(changed.path("data_retention").asText()).isEqualTo("90d");
    }

    @Test
    void put_data_stream_lifecycle_error_carries_the_elasticsearch_response() throws InterruptedException {
        client
            .putDataStreamLifecycle("gravitee-missing-stream", "{\"data_retention\":\"90d\"}")
            .test()
            .await()
            .assertError(
                error ->
                    error instanceof ElasticsearchException elasticsearchException &&
                    Integer.valueOf(404).equals(elasticsearchException.getStatusCode()) &&
                    error.getMessage().contains("'gravitee-missing-stream'") &&
                    error.getMessage().contains("status[404]")
            );
    }

    private void createDataStream(String name) throws InterruptedException {
        client.putIndexTemplate(name, TEMPLATE.formatted(name)).test().await().assertComplete();
        client
            .bulk(Buffer.buffer("{\"create\":{\"_index\":\"" + name + "\"}}\n{\"@timestamp\":\"2026-11-18T10:00:00Z\"}\n"))
            .test()
            .await()
            .assertNoErrors();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public Vertx vertx() {
            return Vertx.vertx();
        }

        @Bean
        public Client client(HttpClientConfiguration clientConfiguration) {
            return new HttpClient(clientConfiguration);
        }

        @Bean
        public HttpClientConfiguration configuration(ElasticsearchContainer elasticsearch) {
            final HttpClientConfiguration configuration = new HttpClientConfiguration();
            configuration.setEndpoints(List.of(new Endpoint("http://" + elasticsearch.getHttpHostAddress())));
            return configuration;
        }

        @Bean(destroyMethod = "close")
        public ElasticsearchContainer elasticsearch() {
            final ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
                "docker.elastic.co/elasticsearch/elasticsearch:" + ELASTICSEARCH_VERSION
            ).withEnv("xpack.security.enabled", "false");
            elasticsearch.start();
            return elasticsearch;
        }
    }
}
