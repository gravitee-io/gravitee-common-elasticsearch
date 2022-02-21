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
package io.gravitee.elasticsearch.client;

import io.gravitee.elasticsearch.exception.ElasticsearchException;
import io.gravitee.elasticsearch.model.*;
import io.gravitee.elasticsearch.model.bulk.BulkResponse;
import io.gravitee.elasticsearch.version.ElasticsearchInfo;
import io.gravitee.elasticsearch.version.Version;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roberto MOZZICATO (roberto.mozzicato at corvallis.it)
 */
public class MockClient implements Client {

    @Override
    public Single<ElasticsearchInfo> getInfo() throws ElasticsearchException {
        ElasticsearchInfo info = new ElasticsearchInfo();
        info.setClusterName("MockES");
        info.setName("MockES");
        info.setStatus(200);
        Version v = new Version();
        v.setNumber("7.4");
        info.setVersion(v);
        return Single.just(info);
    }

    @Override
    public Single<Health> getClusterHealth() {
        return Single.just(new Health());
    }

    @Override
    public Single<BulkResponse> bulk(final List<io.vertx.core.buffer.Buffer> data) {
        BulkResponse res = new BulkResponse();
        res.setTook(10L);
        res.setErrors(false);
        res.setItems(new ArrayList<>());
        return Single.just(res);
    }

    @Override
    public Completable putTemplate(String templateName, String template) {
        return Completable.complete();
    }

    @Override
    public Single<CountResponse> count(final String indexes, final String type, final String query) {
        CountResponse res = new CountResponse();
        res.setCount(0L);
        return Single.just(res);
    }

    @Override
    public Single<SearchResponse> search(final String indexes, final String type, final String query) {
        SearchResponse res = new SearchResponse();
        res.setTook(54L);
        res.setTimedOut(false);
        SearchHits hits = new SearchHits();
        hits.setHits(new ArrayList<SearchHit>());
        hits.setTotal(new TotalHits(0));
        res.setSearchHits(hits);
        return Single.just(res);
    }

    @Override
    public Completable putPipeline(String pipelineName, String pipeline) {
        return Completable.complete();
    }
}
