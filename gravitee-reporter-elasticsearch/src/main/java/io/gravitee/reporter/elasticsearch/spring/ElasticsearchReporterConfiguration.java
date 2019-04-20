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
package io.gravitee.reporter.elasticsearch.spring;

import io.gravitee.elasticsearch.client.Client;
import io.gravitee.elasticsearch.client.http.HttpClient;
import io.gravitee.elasticsearch.client.http.HttpClientConfiguration;
import io.gravitee.elasticsearch.templating.freemarker.FreeMarkerComponent;
import io.gravitee.reporter.elasticsearch.config.ReporterConfiguration;
import io.gravitee.reporter.elasticsearch.config.PipelineConfiguration;
import io.vertx.reactivex.core.Vertx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchReporterConfiguration {

    @Bean
    public Vertx vertxRx(io.vertx.core.Vertx vertx) {
        return Vertx.newInstance(vertx);
    }

    @Bean
    public Client httpClient(ReporterConfiguration reporterConfiguration) {
        HttpClientConfiguration clientConfiguration = new HttpClientConfiguration();
        clientConfiguration.setEndpoints(reporterConfiguration.getEndpoints());
        clientConfiguration.setUsername(reporterConfiguration.getUsername());
        clientConfiguration.setPassword(reporterConfiguration.getPassword());
        clientConfiguration.setRequestTimeout(reporterConfiguration.getRequestTimeout());
        return new HttpClient(clientConfiguration);
    }

    @Bean 
    public ReporterConfiguration configuration(){
    	return new ReporterConfiguration();
    }

    @Bean
    public PipelineConfiguration pipelineConfiguration() {
        return new PipelineConfiguration();
    }

    @Bean
    public FreeMarkerComponent freeMarkerComponent() {
        return new FreeMarkerComponent();
    }
}
