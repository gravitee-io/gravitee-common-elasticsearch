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
import io.gravitee.elasticsearch.exception.ElasticsearchException;
import io.gravitee.elasticsearch.model.CountResponse;
import io.gravitee.elasticsearch.model.Health;
import io.gravitee.elasticsearch.model.SearchResponse;
import io.gravitee.elasticsearch.model.bulk.BulkResponse;
import io.gravitee.elasticsearch.version.ElasticsearchInfo;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface Client {
    Single<ElasticsearchInfo> getInfo() throws ElasticsearchException;
    Single<Health> getClusterHealth();
    Single<List<String>> getFieldTypes(String indexName, String fieldName);

    /**
     * The field capabilities of the given indexes ({@code _field_caps}): each mapped field matching the given
     * fields, wildcards allowed, with the types it has across the indexes (more than one when they disagree).
     * The answer may include parent objects, with the type {@code object}. Unavailable indexes are ignored.
     *
     * <p>An implementation without support keeps this default, which fails with an
     * {@link UnsupportedOperationException}: callers treat the failure as field capabilities being unavailable.
     *
     * @param indexes indexes names separated by comma, wildcards allowed
     * @param fields field names separated by comma, wildcards allowed
     * @return the types of each matching field, by field name
     */
    default Single<Map<String, Set<String>>> getFieldCapabilities(String indexes, String fields) {
        return Single.error(new UnsupportedOperationException("field capabilities are not supported by this client"));
    }

    default Single<BulkResponse> bulk(List<Buffer> data) {
        return bulk(data, false);
    }

    default Single<BulkResponse> bulk(List<Buffer> data, boolean forceRefresh) {
        Buffer payload = Buffer.buffer();
        data.forEach(payload::appendBuffer);
        return bulk(payload, forceRefresh);
    }

    default Single<BulkResponse> bulk(Buffer data) {
        return bulk(data, false);
    }

    Single<BulkResponse> bulk(Buffer data, boolean forceRefresh);

    Completable putTemplate(String templateName, String template);
    Completable putIndexTemplate(String templateName, String template);
    Completable putPipeline(String templateName, String template);

    /**
     * The definition of a composable index template ({@code GET _index_template/<name>}): its index patterns, its
     * {@code template} and its {@code _meta}.
     *
     * <p>An implementation without support keeps this default, which fails with an {@link UnsupportedOperationException}.
     *
     * @param templateName the exact name of the template, without wildcards
     * @return the definition of the template, or empty when it does not exist
     */
    default Maybe<JsonNode> getIndexTemplate(String templateName) {
        return Maybe.error(new UnsupportedOperationException("index templates are not readable with this client"));
    }

    /**
     * The lifecycle of a data stream ({@code GET _data_stream/<name>/_lifecycle}), such as
     * {@code {"enabled": true, "data_retention": "45d"}}. Needs Elasticsearch 8.14 or later.
     *
     * <p>An implementation without support keeps this default, which fails with an {@link UnsupportedOperationException}.
     *
     * @param dataStreamName the exact name of the data stream, without wildcards
     * @return the lifecycle of the data stream, or empty when the data stream does not exist or has no lifecycle (managed by
     * ILM, or created from a template without {@code template.lifecycle})
     */
    default Maybe<JsonNode> getDataStreamLifecycle(String dataStreamName) {
        return Maybe.error(new UnsupportedOperationException("data stream lifecycles are not readable with this client"));
    }

    /**
     * Replaces the lifecycle of an existing data stream ({@code PUT _data_stream/<name>/_lifecycle}). Needs Elasticsearch 8.14
     * or later.
     *
     * <p>An implementation without support keeps this default, which fails with an {@link UnsupportedOperationException}.
     *
     * @param dataStreamName the exact name of the data stream, without wildcards
     * @param lifecycle the lifecycle, such as {@code {"data_retention": "45d"}}
     */
    default Completable putDataStreamLifecycle(String dataStreamName, String lifecycle) {
        return Completable.error(new UnsupportedOperationException("data stream lifecycles are not writable with this client"));
    }

    Single<SearchResponse> search(String indexes, String type, String query);
    Single<CountResponse> count(String indexes, String type, String query);

    Maybe<JsonNode> getAlias(String aliasName);
    Completable createIndexWithAlias(String indexName, String template);
}
