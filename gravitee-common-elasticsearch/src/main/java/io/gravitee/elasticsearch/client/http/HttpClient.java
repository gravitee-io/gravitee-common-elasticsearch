/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.elasticsearch.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.common.http.MediaType;
import io.gravitee.elasticsearch.client.Client;
import io.gravitee.elasticsearch.config.Endpoint;
import io.gravitee.elasticsearch.exception.ElasticsearchException;
import io.gravitee.elasticsearch.model.Health;
import io.gravitee.elasticsearch.model.SearchResponse;
import io.gravitee.elasticsearch.model.bulk.BulkResponse;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class HttpClient implements Client {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private static final String HTTPS_SCHEME = "https";
    private static final String CONTENT_TYPE = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private static final String URL_ROOT = "/";
    private static final String URL_STATE_CLUSTER = "/_cluster/health";
    private static final String URL_BULK = "/_bulk";
    private static final String URL_TEMPLATE = "/_template";
    private static final String URL_INGEST = "/_ingest/pipeline";
    private static final String URL_SEARCH = "/_search?ignore_unavailable=true";

    @Autowired
    private Vertx vertx;

    /**
     * Configuration of Elasticsearch (cluster name, addresses, ...)
     */
    private HttpClientConfiguration configuration;

    /**
     * HTTP client.
     */
    private io.vertx.reactivex.core.http.HttpClient httpClient;

    /**
     * Authorization header if Elasticsearch is protected.
     */
    private String authorizationHeader;

    private final ObjectMapper mapper = new ObjectMapper();

    public HttpClient() {
        this(new HttpClientConfiguration());
    }

    public HttpClient(final HttpClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void initialize() {
        if (! configuration.getEndpoints().isEmpty()) {
            final Endpoint endpoint = configuration.getEndpoints().get(0);
            final URI elasticEdpt = URI.create(endpoint.getUrl());

            HttpClientOptions options = new HttpClientOptions()
                    .setDefaultHost(elasticEdpt.getHost())
                    .setDefaultPort(elasticEdpt.getPort() != -1 ? elasticEdpt.getPort() :
                            (HTTPS_SCHEME.equals(elasticEdpt.getScheme()) ? 443 : 80));

            if (HTTPS_SCHEME.equals(elasticEdpt.getScheme())) {
                options
                        .setSsl(true)
                        .setTrustAll(true);
            }

            this.httpClient = vertx.createHttpClient(options);

            // Read configuration to authenticate calls to Elasticsearch (basic authentication only)
            if (this.configuration.getUsername() != null) {
                this.authorizationHeader = this.initEncodedAuthorization(this.configuration.getUsername(),
                        this.configuration.getPassword());
            }
        }
    }

    @Override
    public Single<Integer> getVersion() throws ElasticsearchException {
        HttpClientRequest req = httpClient.get(URL_ROOT);
        return doRequest(req)
                .map(response -> mapper.readTree(response.body()).path("version").path("number").asText())
                .map(sVersion -> {
                    float result = Float.valueOf(sVersion.substring(0, 3));
                    int version = Integer.valueOf(sVersion.substring(0, 1));
                    if (result < 2) {
                        logger.warn("Please upgrade to Elasticsearch 2 or later. version={}", version);
                    }
                    return version;
                });
    }

    /**
     * Get the cluster health
     *
     * @return the cluster health
     * @throws ElasticsearchException error occurs during ES call
     */
    @Override
    public Single<Health> getClusterHealth() {
        HttpClientRequest req = httpClient.get(URL_STATE_CLUSTER);

        return doRequest(req)
                .map(response -> mapper.readValue(response.body(), Health.class));
    }

    @Override
    public Single<BulkResponse> bulk(final List<String> data) {
        if (data != null && !data.isEmpty()) {
            String content = data.stream().collect(Collectors.joining());

            HttpClientRequest req = httpClient.post(URL_BULK);
            req.putHeader(HttpHeaders.CONTENT_TYPE, "application/x-ndjson");

            return doRequest(req, content)
                    .map(response -> {
                        if (response.statusCode() != HttpStatusCode.OK_200) {
                            logger.error("Unable to bulk index data: status[{}] data[{}] response[{}]",
                                    response.statusCode(), content, response.body());
                            throw new ElasticsearchException("Unable to bulk index data");
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("ES response: {}", response.body());
                        }

                        return mapper.readValue(response.body(), BulkResponse.class);
                    });
        }

        return Single.never();
    }

    @Override
    public Single<Boolean> putTemplate(String templateName, String template) {
        HttpClientRequest req = httpClient
                .put(URL_TEMPLATE + '/' + templateName)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        return doRequest(req, template)
                .map(response -> {
                    if (response.statusCode() != HttpStatusCode.OK_200) {
                        logger.error("Unable to put template mapping: status[{}] template[{}] response[{}]",
                                response.statusCode(), template, response.body());
                        throw new ElasticsearchException("Unable to put template mapping");
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("ES response: {}", response.body());
                    }

                    return true;
                });
    }

    /**
     * Perform an HTTP search query
     * @param indexes indexes names. If null search on all indexes
     * @param type document type separated by comma. If null search on all types
     * @param query json body query
     * @return elasticsearch response
     */
    public Single<SearchResponse> search(final String indexes, final String type, final String query) {
        // index can be null _search on all index
        final StringBuilder url = new StringBuilder()
                .append('/')
                .append(indexes);

        if (type != null) {
            url.append('/').append(type);
        }

        url.append(URL_SEARCH);

        HttpClientRequest req = httpClient
                .post(url.toString())
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        return doRequest(req, query)
                .map(response -> {
                    if (response.statusCode() != HttpStatusCode.OK_200) {
                        logger.error("Unable to search: status[{}] query[{}] response[{}]",
                                response.statusCode(), query, response.body());
                        throw new ElasticsearchException("Unable to search");
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("ES response: {}", response.body());
                    }
                    return mapper.readValue(response.body(), SearchResponse.class);
                });
    }

    private Single<Response> doRequest(final HttpClientRequest request) {
        return doRequest(request, null);
    }

    private Single<Response> doRequest(final HttpClientRequest request, final String body) {
        logger.debug("Calling {} {}, with body {}", request.method(), request.absoluteURI(), body);
        addCommonHeaders(request);

        return Single.<Response>create(singleEmitter ->
                request
                        .exceptionHandler(singleEmitter::onError)
                        .toFlowable()
                        .doOnSubscribe(subscription -> {
                            if (body == null) {
                                request.end();
                            } else {
                                request.end(body);
                            }
                        })
                        .flatMapSingle(new Function<HttpClientResponse, Single<Response>>() {
                            @Override
                            public Single<Response> apply(HttpClientResponse resp) throws Exception {
                                return Single.create(sub -> {
                                    resp.exceptionHandler(throwable -> {
                                        logger.error("An error occurs while calling Elasticsearch {} {}",
                                                request.getRawMethod(), request.absoluteURI(), throwable);

                                        singleEmitter.onError(throwable);
                                    });

                                    resp.bodyHandler(body -> singleEmitter.onSuccess(new Response(resp, body)));
                                });
                            }
                        })
                        .subscribe())
                .doOnError(throwable -> logger.error("An error occurs while calling Elasticsearch", throwable));
    }

    /**
     * Create the Basic HTTP auth
     *
     * @param username
     *            username
     * @param password
     *            password
     * @return Basic auth string
     */
    private String initEncodedAuthorization(final String username, final String password) {
        final String auth = username + ":" + password;
        final String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }

    /**
     * Add the common header to call Elasticsearch.
     *
     * @param request
     *            the HTTP Client request
     * @return HTTP Client request
     */
    private void addCommonHeaders(final HttpClientRequest request) {
        request
                .putHeader(HttpHeaders.ACCEPT, CONTENT_TYPE)
                .putHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());

        // Basic authentication
        if (this.authorizationHeader != null) {
            request.putHeader(HttpHeaders.AUTHORIZATION, this.authorizationHeader);
        }
    }

    public void setConfiguration(HttpClientConfiguration configuration) {
        this.configuration = configuration;
    }

    private class Response {
        final private HttpClientResponse response;
        final private Buffer body;

        Response(HttpClientResponse theResponse, Buffer theBody) {
            response = theResponse;
            body = theBody;
        }

        int statusCode() {
            return response.statusCode();
        }

        String body() {
            return body.toString();
        }
    }
}
