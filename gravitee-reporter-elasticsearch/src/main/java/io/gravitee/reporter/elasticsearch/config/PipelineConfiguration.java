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
package io.gravitee.reporter.elasticsearch.config;

import io.gravitee.elasticsearch.templating.freemarker.FreeMarkerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Guillaume Gillon
 */
public class PipelineConfiguration {

    private final Logger LOGGER = LoggerFactory.getLogger(PipelineConfiguration.class);

    private static final List<String> ingestManaged = Collections.singletonList("geoip");

    /**
     * Templating tool.
     */
    @Autowired
    private FreeMarkerComponent freeMarkerComponent;

    private final String pipeline = "gravitee_pipeline";

    private boolean valid = false;

    public String createPipeline(int majorVersion) {
        String template = null;

        if (majorVersion < 5) {
            LOGGER.error("Ingest is not managed for the elasticsearch version below 5");
            return null;
        }

        String processors = this.ingestManaged.stream()
                .map(ingestPlug -> this.freeMarkerComponent.generateFromTemplate(ingestPlug + ".ftl"))
                .collect(
                        Collectors.joining(","));

        Map<String,Object> processorsMap = new HashMap<>(1);
        processorsMap.put("processors", processors);
        template = this.freeMarkerComponent.generateFromTemplate("pipeline.ftl", processorsMap);

        return template;
    }

    public String getIngestManaged() {
        return ingestManaged.stream().collect(Collectors.joining(","));
    }

    public List<String> getIngestPlugins() {
        return this.ingestManaged;
    }

    public String getPipelineName() { return this.pipeline; }

    public String getPipeline() { return valid ? this.pipeline : null; }

    public void valid() {
        this.valid = true;
    }
}
