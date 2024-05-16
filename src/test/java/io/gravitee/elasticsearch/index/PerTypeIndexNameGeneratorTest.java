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
package io.gravitee.elasticsearch.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.elasticsearch.utils.Type;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Aurelien PACAUD (aurelien.pacaud at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PerTypeIndexNameGeneratorTest {

    @ParameterizedTest
    @MethodSource
    void shouldGenerateIndexNameWithPlaceholder(
        String indexName,
        Map<String, String> parameters,
        String[] clusters,
        String expectedIndexName
    ) {
        assertThat(new PerTypeIndexNameGenerator(indexName).getIndexName(parameters, Type.REQUEST, 0, 0, clusters))
            .isEqualTo(expectedIndexName);
    }

    private static Stream<Arguments> shouldGenerateIndexNameWithPlaceholder() {
        return Stream.of(
            Arguments.of("gravitee", Map.of(), null, "gravitee-request-1970.01.01"),
            Arguments.of(
                "gravitee",
                Map.of(),
                new String[] { "europe", "asia" },
                "europe:gravitee-request-1970.01.01,asia:gravitee-request-1970.01.01"
            ),
            Arguments.of(
                "gravitee-{firstPlaceholder}-{secondPlaceholder}",
                Map.of(),
                null,
                "gravitee-{firstPlaceholder}-{secondPlaceholder}-request-1970.01.01"
            ),
            Arguments.of("gravitee-{firstPlaceholder}", Map.of("firstPlaceholder", "VALUE1"), null, "gravitee-value1-request-1970.01.01"),
            Arguments.of(
                "gravitee-{firstPlaceholder}-{secondPlaceholder}",
                Map.of("firstPlaceholder", "value1", "secondPlaceholder", "value2"),
                null,
                "gravitee-value1-value2-request-1970.01.01"
            ),
            Arguments.of(
                "gravitee-{firstPlaceholder}-{placeholderNotFound}",
                Map.of("firstPlaceholder", "value1"),
                null,
                "gravitee-value1-{placeholderNotFound}-request-1970.01.01"
            ),
            Arguments.of(
                "gravitee-{firstPlaceholder}",
                Map.of("firstPlaceholder", "value1"),
                new String[] { "europe", "asia" },
                "europe:gravitee-value1-request-1970.01.01,asia:gravitee-value1-request-1970.01.01"
            ),
            Arguments.of(
                "gravitee-{firstPlaceholder}-{placeholderNotFound}",
                Map.of("firstPlaceholder", "value1"),
                new String[] { "europe", "asia" },
                "europe:gravitee-value1-{placeholderNotFound}-request-1970.01.01,asia:gravitee-value1-{placeholderNotFound}-request-1970.01.01"
            )
        );
    }
}
