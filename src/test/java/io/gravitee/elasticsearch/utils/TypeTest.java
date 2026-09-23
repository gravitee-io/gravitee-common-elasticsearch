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
package io.gravitee.elasticsearch.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * @author GraviteeSource Team
 */
public class TypeTest {

    @Test
    public void should_list_every_declared_type_in_the_TYPES_array() {
        assertThat(Type.TYPES).containsExactlyInAnyOrder(Type.values());
    }

    @Test
    public void should_expose_decisions_as_a_data_stream() {
        assertThat(Type.DECISIONS.getType()).isEqualTo("decisions");
        assertThat(Type.DECISIONS.isDataStream()).isTrue();
    }

    @Test
    void decisions_is_a_data_stream_and_authz_decisions_is_gone() {
        assertThat(Type.DECISIONS.getType()).isEqualTo("decisions");
        assertThat(Type.DECISIONS.isDataStream()).isTrue();
        assertThat(Arrays.stream(Type.values()).map(Type::getType)).doesNotContain("authz-decisions");
    }
}
