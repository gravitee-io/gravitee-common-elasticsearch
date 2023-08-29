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

import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DateUtilsTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+01:00"));
    }

    @ParameterizedTest
    @MethodSource("provideDates")
    void shouldComputeIndices(long from, long to, List<String> expected) {
        List<String> indices = DateUtils.rangedIndices(from, to);
        assertThat(indices).isEqualTo(expected);
    }

    private static Stream<Arguments> provideDates() {
        return Stream.of(
            // from 5 JAN to 10 JAN
            Arguments.of(
                1672873200000L,
                1673305259000L,
                List.of("2023.01.05", "2023.01.06", "2023.01.07", "2023.01.08", "2023.01.09", "2023.01.10")
            ),
            // from 1 JAN to 31 JAN
            Arguments.of(1672527600000L, 1675119659000L, List.of("2023.01.*")),
            // from 1 JAN to 4 MARCH
            Arguments.of(
                1672527600000L,
                1677884459000L,
                List.of("2023.01.*", "2023.02.*", "2023.03.01", "2023.03.02", "2023.03.03", "2023.03.04")
            ),
            // from 1 JAN to 31 MARCH
            Arguments.of(1672527600000L, 1680278352000L, List.of("2023.01.*", "2023.02.*", "2023.03.*")),
            // form 28 JAN to 4 MARCH
            Arguments.of(
                1674860400000L,
                1677884459000L,
                List.of(
                    "2023.01.28",
                    "2023.01.29",
                    "2023.01.30",
                    "2023.01.31",
                    "2023.02.*",
                    "2023.03.01",
                    "2023.03.02",
                    "2023.03.03",
                    "2023.03.04"
                )
            ),
            // from 25 DEC 2022 to 2 FEB 2023
            Arguments.of(
                1671966915000L,
                1675336515000L,
                List.of(
                    "2022.12.25",
                    "2022.12.26",
                    "2022.12.27",
                    "2022.12.28",
                    "2022.12.29",
                    "2022.12.30",
                    "2022.12.31",
                    "2023.01.*",
                    "2023.02.01",
                    "2023.02.02"
                )
            ),
            // from 1 DEC 2022 to 31 MARCH 2023
            Arguments.of(1669893315000L, 1680261315000L, List.of("2022.12.*", "2023.01.*", "2023.02.*", "2023.03.*"))
        );
    }
}
