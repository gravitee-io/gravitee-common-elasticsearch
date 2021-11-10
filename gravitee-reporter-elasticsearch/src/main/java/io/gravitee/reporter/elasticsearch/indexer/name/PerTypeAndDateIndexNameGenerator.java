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
package io.gravitee.reporter.elasticsearch.indexer.name;

import javax.annotation.PostConstruct;
import java.time.Instant;

/**
 * @author GraviteeSource Team
 */
public class PerTypeAndDateIndexNameGenerator extends AbstractPerTypeIndexNameGenerator {

    private String indexNameTemplate;

    @PostConstruct
    public void initialize() {
        indexNameTemplate = configuration.getIndexName() + "-%s-%s";
    }

    @Override
    public String generate(String type, Instant timestamp) {
        return String.format(indexNameTemplate, type, sdf.format(timestamp));
    }
}
