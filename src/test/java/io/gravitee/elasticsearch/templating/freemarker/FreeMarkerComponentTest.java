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
package io.gravitee.elasticsearch.templating.freemarker;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the template.
 *
 * @author Guillaume Waignier
 * @author Sebastien Devaux
 *
 */
public class FreeMarkerComponentTest {

    FreeMarkerComponent freeMarkerComponent = new FreeMarkerComponent();

    @BeforeEach
    void init() throws IOException {
        freeMarkerComponent.afterPropertiesSet();
    }

    @Test
    void testGenerateFromTemplateWithoutData() {
        final String result = this.freeMarkerComponent.generateFromTemplate("template.ftl");
        assertThat(result).isEqualTo("test");
    }

    @Test
    void testGenerateFromTemplateWithData() {
        final Map<String, Object> data = new HashMap<>();
        data.put("data", "test");

        final String result = this.freeMarkerComponent.generateFromTemplate("templateWithData.ftl", data);
        assertThat(result).isEqualTo("test with data : test");
    }
}
