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
package io.gravitee.elasticsearch.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AggregationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("countsAboveFloatPrecision")
    void should_keep_metric_value_exact_above_float_precision(long count) throws Exception {
        var json = "{\"value\":" + count + "}";

        var aggregation = MAPPER.readValue(json, Aggregation.class);

        assertThat(aggregation.getValue().longValue()).isEqualTo(count);
    }

    @ParameterizedTest
    @MethodSource("countsAboveFloatPrecision")
    void should_keep_stats_count_exact_above_float_precision(long count) throws Exception {
        var json = "{\"count\":" + count + ",\"min\":1,\"max\":2,\"avg\":1.5,\"sum\":" + count + "}";

        var aggregation = MAPPER.readValue(json, Aggregation.class);

        assertThat(aggregation.getCount().longValue()).isEqualTo(count);
        assertThat(aggregation.getSum().longValue()).isEqualTo(count);
    }

    private static Stream<Long> countsAboveFloatPrecision() {
        // 2^24 is the largest integer a float represents exactly; beyond it the spacing between
        // representable values grows to 2, then 4, then 8...
        return Stream.of(16_777_217L, 20_000_001L, 50_000_003L, 120_812_767L, 999_999_999L);
    }

    @Test
    void should_keep_a_flat_total_consistent_with_the_sum_of_its_buckets() throws Exception {
        var flatTotal = MAPPER.readValue("{\"value\":120812767}", Aggregation.class);
        var perBucketTotals = Stream
            .of("{\"value\":120800000}", "{\"value\":12767}")
            .map(json -> {
                try {
                    return MAPPER.readValue(json, Aggregation.class);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            })
            .mapToLong(aggregation -> aggregation.getValue().longValue())
            .sum();

        assertThat(flatTotal.getValue().longValue()).isEqualTo(perBucketTotals);
    }

    @Test
    void should_deserialize_percentile_values() throws Exception {
        var json = "{\"values\":{\"50.0\":1234.5,\"99.0\":16777217.0}}";

        var aggregation = MAPPER.readValue(json, Aggregation.class);

        assertThat(aggregation.getValues()).containsEntry("50.0", 1234.5).containsEntry("99.0", 16_777_217.0);
    }
}
