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

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IndexNameUtilsTest {

    @ParameterizedTest
    @MethodSource
    void format(Map<String, String> parameters, String template, String expected) {
        assertThat(IndexNameUtils.format(template, parameters)).isEqualTo(expected);
    }

    private static Stream<Arguments> format() {
        return Stream.of(
            Arguments.of(Map.of("placeHolder", "value1"), "string-{placeHolder}", "string-value1"),
            Arguments.of(
                Map.of("firstPlaceHolder", "value1", "secondPlaceHolder", "value2"),
                "{firstPlaceHolder}-{secondPlaceHolder}",
                "value1-value2"
            ),
            Arguments.of(
                Map.of("firstPlaceHolder", "value1", "secondPlaceHolder", "value2"),
                "{firstPlaceHolder}-{placeHolderNotFound}",
                "value1-{placeHolderNotFound}"
            )
        );
    }
}
