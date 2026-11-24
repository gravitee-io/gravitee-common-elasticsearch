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
package io.gravitee.elasticsearch.client.http;

import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.TrustOptions;
import java.util.Locale;

/**
 * Certificates the client trusts when it verifies the certificate of an {@code https} Elasticsearch endpoint.
 * Used only when {@link HttpClientConfiguration#isTrustAll()} is {@code false}; without it, the JVM default truststore applies.
 *
 * @author GraviteeSource Team
 */
public class HttpClientTrustStoreConfiguration {

    public static final String JKS_TYPE = "jks";
    public static final String PEM_TYPE = "pem";
    public static final String PFX_TYPE = "pfx";
    public static final String PKCS12_TYPE = "pkcs12";

    /**
     * One of {@code jks}, {@code pkcs12} (or {@code pfx}) and {@code pem}, case-insensitive.
     */
    private String type;

    /**
     * Path of the truststore. For {@code pem}, a file holding one or more CA certificates.
     */
    private String path;

    /**
     * Password of a {@code jks} or {@code pkcs12} truststore. Unused for {@code pem}.
     */
    private String password;

    public HttpClientTrustStoreConfiguration() {}

    public HttpClientTrustStoreConfiguration(String type, String path, String password) {
        this.type = type;
        this.path = path;
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @throws IllegalArgumentException when the type is missing or unknown, or the path is missing
     */
    public TrustOptions getVertxWebClientTrustOptions() {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("The Elasticsearch truststore path is required");
        }
        final String normalizedType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedType) {
            case JKS_TYPE -> new JksOptions().setPath(path).setPassword(password);
            case PKCS12_TYPE, PFX_TYPE -> new PfxOptions().setPath(path).setPassword(password);
            case PEM_TYPE -> new PemTrustOptions().addCertPath(path);
            default -> throw new IllegalArgumentException(
                "Unknown Elasticsearch truststore type '" + type + "', expected one of jks, pkcs12, pfx or pem"
            );
        };
    }
}
