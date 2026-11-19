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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.gravitee.elasticsearch.client.http.HttpClient;
import io.gravitee.elasticsearch.client.http.HttpClientConfiguration;
import io.gravitee.elasticsearch.config.Endpoint;
import io.gravitee.elasticsearch.exception.ElasticsearchException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * How {@link HttpClient#getAlias(String)} reads each answer of Elasticsearch, against a local HTTP server.
 */
class HttpClientAliasTest {

    private static final String ALIAS = "gravitee-request";
    private static final String ALIAS_BODY = "{\"gravitee-request-000001\":{\"aliases\":{\"gravitee-request\":{\"is_write_index\":true}}}}";

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Vertx vertx;
    private HttpServer server;
    private volatile int status;
    private volatile String body;

    @BeforeEach
    void startServer() {
        logs.start();
        clientLogger().addAppender(logs);
        vertx = Vertx.vertx();
        server = vertx
            .createHttpServer()
            .requestHandler(request ->
                request.response().setStatusCode(status).putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json").end(body)
            )
            .rxListen(0)
            .blockingGet();
    }

    @AfterEach
    void stopServer() {
        clientLogger().detachAppender(logs);
        vertx.close().blockingAwait();
    }

    @Test
    void should_get_the_alias_when_elasticsearch_finds_it() throws InterruptedException {
        answer(200, ALIAS_BODY);

        client()
            .getAlias(ALIAS)
            .timeout(5, TimeUnit.SECONDS)
            .test()
            .await()
            .assertNoErrors()
            .assertValue(alias -> alias.has("gravitee-request-000001"));
    }

    @Test
    void should_get_nothing_and_log_at_debug_when_the_alias_does_not_exist() throws InterruptedException {
        answer(404, "{\"error\":\"alias [gravitee-request] missing\",\"status\":404}");

        client().getAlias(ALIAS).timeout(5, TimeUnit.SECONDS).test().await().assertNoErrors().assertComplete().assertNoValues();

        assertThat(logs.list)
            .filteredOn(event -> event.getFormattedMessage().contains("Alias [" + ALIAS + "] not found"))
            .singleElement()
            .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.DEBUG));
    }

    @ParameterizedTest(name = "status {0}")
    @ValueSource(ints = { 403, 500 })
    void should_fail_with_the_status_when_elasticsearch_cannot_tell_whether_the_alias_exists(int answeredStatus)
        throws InterruptedException {
        answer(answeredStatus, "{\"error\":{\"type\":\"security_exception\"},\"status\":" + answeredStatus + "}");

        client()
            .getAlias(ALIAS)
            .timeout(5, TimeUnit.SECONDS)
            .test()
            .await()
            .assertError(
                error ->
                    error instanceof ElasticsearchException elasticsearchException &&
                    Integer.valueOf(answeredStatus).equals(elasticsearchException.getStatusCode()) &&
                    error.getMessage().contains("'" + ALIAS + "'") &&
                    error.getMessage().contains("status[" + answeredStatus + "]")
            );
    }

    private void answer(int status, String body) {
        this.status = status;
        this.body = body;
    }

    private HttpClient client() {
        final HttpClientConfiguration configuration = new HttpClientConfiguration();
        configuration.setEndpoints(List.of(new Endpoint("http://localhost:" + server.actualPort())));
        configuration.setRequestTimeout(3_000L);
        final HttpClient client = new HttpClient(configuration);
        ReflectionTestUtils.setField(client, "vertx", vertx);
        ReflectionTestUtils.setField(client, "enabled", true);
        client.initialize();
        return client;
    }

    private static Logger clientLogger() {
        final Logger logger = (Logger) LoggerFactory.getLogger(HttpClient.class);
        logger.setLevel(Level.DEBUG);
        return logger;
    }
}
