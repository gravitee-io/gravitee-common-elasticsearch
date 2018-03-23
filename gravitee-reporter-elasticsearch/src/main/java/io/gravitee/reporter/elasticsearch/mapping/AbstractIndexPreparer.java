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
package io.gravitee.reporter.elasticsearch.mapping;

import io.gravitee.elasticsearch.client.Client;
import io.gravitee.elasticsearch.templating.freemarker.FreeMarkerComponent;
import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import io.gravitee.reporter.elasticsearch.config.PipelineConfiguration;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
abstract class AbstractIndexPreparer implements IndexPreparer {

    /** Logger. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ReporterConfiguration configuration;

    /**
     * Configuration of pipelineConfiguration
     */
    @Autowired
    protected PipelineConfiguration pipelineConfiguration;

    @Autowired
    protected FreeMarkerComponent freeMarkerComponent;

    @Autowired
    protected Client client;

    protected Map<String, Object> getTemplateData() {
        final Map<String, Object> data = new HashMap<>();

        data.put("indexName", this.configuration.getIndexName());
        data.put("numberOfShards", this.configuration.getNumberOfShards());
        data.put("numberOfReplicas", this.configuration.getNumberOfReplicas());

        return data;
    }

    @Override
    public Single<Boolean> ensurePlugins() {
        return null;
    }

    /**
     * Put the ingest template.
     *
     * @throws TechnicalException
     *             when a problem occur during the http call
     */
    /*
    private void ensureIngestPlugins() throws TechnicalException {
        try {
            String pipelineTemplate = pipelineConfiguration.createPipeline(client.getVersion());

            if (pipelineTemplate != null && pipelineConfiguration.getPipelineName() != null) {
                logger.debug("PUT ingest pipeline template : {}", pipelineTemplate);

                ElasticHttpClient.ElasticResponse response = client
                        .put(URL_INGEST + "/" + pipelineConfiguration.getPipelineName(), pipelineTemplate)
                        .blockingGet();

                String body = response.body();

                if (response.statusCode() != HttpStatusCode.OK_200) {
                    logger.debug("Impossible to call Elasticsearch PUT {}. {}.", URL_INGEST +
                            "/" + pipelineConfiguration.getPipelineName(), body);
                    logger.warn("Impossible to create a pipeline for " + pipelineConfiguration.getIngestManaged());
                } else {
                    logger.info("Manage Ingest pipeline {}", pipelineConfiguration.getIngestPlugins().toString());
                    this.pipelineConfiguration.valid();
                }
                logger.debug("Response of ES for PUT {} : {}",  URL_INGEST + "/" + pipelineConfiguration.getPipelineName() , body);
            }

        } catch (final Exception e) {
            logger.error("Impossible to call ingest pipeline " + pipelineConfiguration.getPipelineName() +
                    " with ingest plugins " + pipelineConfiguration.getIngestPlugins(), e);
        }
    }
    */

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ReporterConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ReporterConfiguration configuration) {
        this.configuration = configuration;
    }

    public FreeMarkerComponent getFreeMarkerComponent() {
        return freeMarkerComponent;
    }

    public void setFreeMarkerComponent(FreeMarkerComponent freeMarkerComponent) {
        this.freeMarkerComponent = freeMarkerComponent;
    }
}
