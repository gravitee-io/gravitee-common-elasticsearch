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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.gravitee.elasticsearch.client.http.HttpClient;
import io.gravitee.elasticsearch.client.http.HttpClientConfiguration;
import io.gravitee.elasticsearch.config.Endpoint;
import io.gravitee.elasticsearch.version.ElasticsearchInfo;
import io.vertx.core.http.HttpHeaders;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Checks the Authorization header sent to Elasticsearch for each authentication setup, against a local HTTP server.
 *
 * @author GraviteeSource Team
 */
class HttpClientAuthenticationTest {

    private static final String API_KEY = "VnVhQ2ZHY0JDZGJrUW0tZTVhT3g6dWkybHAyYXhUTm1zeWFrdzl0dk5udw==";

    private static final String INFO_RESPONSE = """
        {
          "name": "node-1",
          "cluster_name": "gravitee",
          "cluster_uuid": "uuid",
          "version": { "number": "8.17.2", "build_flavor": "default", "lucene_version": "9.12.0" },
          "tagline": "You Know, for Search"
        }
        """;

    private Vertx vertx;
    private HttpServer server;

    /**
     * Authorization header of each received request; an empty value when the request carried none.
     */
    private final List<Optional<String>> receivedAuthorizations = new CopyOnWriteArrayList<>();

    @BeforeEach
    void startServer() {
        vertx = Vertx.vertx();
        server = vertx
            .createHttpServer()
            .requestHandler(request -> {
                receivedAuthorizations.add(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION.toString())));
                request.response().putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json").end(INFO_RESPONSE);
            })
            .rxListen(0)
            .blockingGet();
    }

    @AfterEach
    void stopServer() {
        vertx.close().blockingAwait();
    }

    @Test
    void sends_an_api_key_authorization_when_only_an_api_key_is_configured() throws InterruptedException {
        HttpClientConfiguration configuration = configuration();
        configuration.setApiKey(API_KEY);

        assertThat(callAndGetAuthorization(configuration)).contains("ApiKey " + API_KEY);
    }

    @Test
    void sends_an_api_key_authorization_when_the_basic_credentials_are_blank() throws InterruptedException {
        HttpClientConfiguration configuration = configuration();
        configuration.setApiKey(API_KEY);
        configuration.setUsername("");
        configuration.setPassword(" ");

        assertThat(callAndGetAuthorization(configuration)).contains("ApiKey " + API_KEY);
    }

    @Test
    void sends_a_basic_authorization_when_only_basic_credentials_are_configured() throws InterruptedException {
        HttpClientConfiguration configuration = configuration();
        configuration.setUsername("elastic");
        configuration.setPassword("changeme");

        String expected = "Basic " + Base64.getEncoder().encodeToString("elastic:changeme".getBytes(StandardCharsets.UTF_8));
        assertThat(callAndGetAuthorization(configuration)).contains(expected);
    }

    @Test
    void sends_a_basic_authorization_when_the_api_key_is_blank() throws InterruptedException {
        HttpClientConfiguration configuration = configuration();
        configuration.setApiKey("");
        configuration.setUsername("elastic");
        configuration.setPassword("changeme");

        assertThat(callAndGetAuthorization(configuration)).hasValueSatisfying(header -> assertThat(header).startsWith("Basic "));
    }

    @Test
    void sends_no_authorization_when_no_credentials_are_configured() throws InterruptedException {
        assertThat(callAndGetAuthorization(configuration())).isEmpty();
    }

    @Test
    void fails_to_initialize_when_both_an_api_key_and_basic_credentials_are_configured() {
        HttpClientConfiguration configuration = configuration();
        configuration.setApiKey(API_KEY);
        configuration.setUsername("elastic");
        configuration.setPassword("changeme");

        HttpClient client = client(configuration);

        assertThatThrownBy(client::initialize)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("apiKey")
            .hasMessageContaining("username/password")
            .hasMessageNotContaining(API_KEY)
            .hasMessageNotContaining("changeme");
    }

    @Test
    void fails_to_initialize_when_both_an_api_key_and_a_password_are_configured() {
        HttpClientConfiguration configuration = configuration();
        configuration.setApiKey(API_KEY);
        configuration.setPassword("changeme");

        assertThatThrownBy(client(configuration)::initialize).isInstanceOf(IllegalArgumentException.class);
    }

    private Optional<String> callAndGetAuthorization(HttpClientConfiguration configuration) throws InterruptedException {
        HttpClient client = client(configuration);
        client.initialize();

        client
            .getInfo()
            .timeout(5, TimeUnit.SECONDS)
            .test()
            .await()
            .assertNoErrors()
            .assertValue(info -> info instanceof ElasticsearchInfo);

        assertThat(receivedAuthorizations).isNotEmpty();
        return receivedAuthorizations.get(0);
    }

    private HttpClientConfiguration configuration() {
        HttpClientConfiguration configuration = new HttpClientConfiguration();
        configuration.setEndpoints(List.of(new Endpoint("http://localhost:" + server.actualPort())));
        configuration.setRequestTimeout(3_000L);
        return configuration;
    }

    private HttpClient client(HttpClientConfiguration configuration) {
        HttpClient client = new HttpClient(configuration);
        ReflectionTestUtils.setField(client, "vertx", vertx);
        ReflectionTestUtils.setField(client, "enabled", true);
        return client;
    }
}
