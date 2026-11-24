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
import io.gravitee.elasticsearch.client.http.HttpClientTrustStoreConfiguration;
import io.gravitee.elasticsearch.config.Endpoint;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Checks which server certificates the client accepts, against a local HTTPS server with a self-signed certificate.
 *
 * @author GraviteeSource Team
 */
class HttpClientTlsTest {

    private static final String INFO_RESPONSE = """
        {
          "name": "node-1",
          "cluster_name": "gravitee",
          "cluster_uuid": "uuid",
          "version": { "number": "8.17.2", "build_flavor": "default", "lucene_version": "9.12.0" },
          "tagline": "You Know, for Search"
        }
        """;

    private static final String PASSWORD = "truststore-password";

    @TempDir
    Path tempDir;

    private Vertx vertx;
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (vertx != null) {
            vertx.close().blockingAwait();
        }
    }

    @Test
    void trusts_any_certificate_by_default() {
        startServer(SelfSignedCertificate.create("localhost"));

        assertConnects(configuration());
    }

    @Test
    void rejects_an_untrusted_certificate_when_trust_all_is_disabled() {
        startServer(SelfSignedCertificate.create("localhost"));
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);

        assertFailsToConnect(configuration);
    }

    @Test
    void trusts_a_certificate_from_a_pem_truststore() {
        SelfSignedCertificate certificate = SelfSignedCertificate.create("localhost");
        startServer(certificate);
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration("pem", certificate.certificatePath(), null));

        assertConnects(configuration);
    }

    @ParameterizedTest
    @ValueSource(strings = { "jks", "pkcs12", "pfx", "PKCS12" })
    void trusts_a_certificate_from_a_keystore_truststore(String type) throws Exception {
        SelfSignedCertificate certificate = SelfSignedCertificate.create("localhost");
        startServer(certificate);
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration(type, truststore(type, certificate), PASSWORD));

        assertConnects(configuration);
    }

    @Test
    void rejects_a_certificate_missing_from_the_truststore() {
        startServer(SelfSignedCertificate.create("localhost"));
        SelfSignedCertificate other = SelfSignedCertificate.create("localhost");
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration("pem", other.certificatePath(), null));

        assertFailsToConnect(configuration);
    }

    @Test
    void rejects_a_trusted_certificate_issued_for_another_host() {
        SelfSignedCertificate certificate = SelfSignedCertificate.create("elasticsearch.example.com");
        startServer(certificate);
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration("pem", certificate.certificatePath(), null));

        assertFailsToConnect(configuration);
    }

    @Test
    void ignores_the_truststore_when_trust_all_is_enabled() {
        startServer(SelfSignedCertificate.create("localhost"));
        SelfSignedCertificate other = SelfSignedCertificate.create("localhost");
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration("pem", other.certificatePath(), null));

        assertConnects(configuration);
    }

    @Test
    void fails_to_initialize_with_an_unknown_truststore_type() {
        startServer(SelfSignedCertificate.create("localhost"));
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration("p12", "/tmp/truststore.p12", PASSWORD));

        assertThatThrownBy(client(configuration)::initialize)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("p12")
            .hasMessageNotContaining(PASSWORD);
    }

    @Test
    void fails_to_initialize_without_a_truststore_path() {
        startServer(SelfSignedCertificate.create("localhost"));
        HttpClientConfiguration configuration = configuration();
        configuration.setTrustAll(false);
        configuration.setTrustStoreConfig(new HttpClientTrustStoreConfiguration("pem", " ", null));

        assertThatThrownBy(client(configuration)::initialize).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("path");
    }

    private void startServer(SelfSignedCertificate certificate) {
        vertx = Vertx.vertx();
        server = vertx
            .createHttpServer(new HttpServerOptions().setSsl(true).setKeyCertOptions(certificate.keyCertOptions()))
            .requestHandler(request ->
                request.response().putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json").end(INFO_RESPONSE)
            )
            .rxListen(0)
            .blockingGet();
    }

    private void assertConnects(HttpClientConfiguration configuration) {
        HttpClient client = client(configuration);
        client.initialize();

        var info = client.getInfo().timeout(5, TimeUnit.SECONDS).blockingGet();

        assertThat(info.getVersion().getNumber()).isEqualTo("8.17.2");
    }

    private void assertFailsToConnect(HttpClientConfiguration configuration) {
        HttpClient client = client(configuration);
        client.initialize();

        assertThatThrownBy(() -> client.getInfo().timeout(5, TimeUnit.SECONDS).blockingGet()).hasStackTraceContaining("SSL");
    }

    private String truststore(String type, SelfSignedCertificate certificate) throws Exception {
        Certificate cert;
        try (InputStream in = Files.newInputStream(Path.of(certificate.certificatePath()))) {
            cert = CertificateFactory.getInstance("X.509").generateCertificate(in);
        }
        KeyStore keyStore = KeyStore.getInstance("jks".equalsIgnoreCase(type) ? "JKS" : "PKCS12");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("elasticsearch", cert);
        Path path = tempDir.resolve("truststore." + type.toLowerCase());
        try (OutputStream out = Files.newOutputStream(path)) {
            keyStore.store(out, PASSWORD.toCharArray());
        }
        return path.toString();
    }

    private HttpClientConfiguration configuration() {
        HttpClientConfiguration configuration = new HttpClientConfiguration();
        configuration.setEndpoints(List.of(new Endpoint("https://localhost:" + server.actualPort())));
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
