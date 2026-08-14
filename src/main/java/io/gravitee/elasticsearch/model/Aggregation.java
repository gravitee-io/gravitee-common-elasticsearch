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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Aggregation implements Serializable {

    /** UID */
    private static final long serialVersionUID = 1L;

    /** If the aggregation is a bucketing one */
    private List<JsonNode> buckets;

    /** If the aggregation is a top hits one */
    private SearchHits hits;

    /** Supports nested aggregations */
    private final Map<String, Aggregation> aggregations = new HashMap<>();

    // Numeric results below are held as doubles on purpose. A float only represents integers
    // exactly below 2^24 (16 777 216), so a counter such as a value_count over a busy index gets
    // silently snapped to the nearest representable value — enough to make a flat total disagree
    // with the sum of the per-bucket totals of the same query. A double is exact up to 2^53,
    // which covers any realistic document count.

    /** If the aggregation is a metric one */
    private Double value;

    /** If the aggregation is a percentile */
    private Map<String, Double> values;

    /** If the aggregation is a stats one */
    private Double count;

    /** If the aggregation is a stats one */
    private Double min;

    /** If the aggregation is a stats one */
    private Double max;

    /** If the aggregation is a stats one */
    private Double avg;

    /** If the aggregation is a stats one */
    private Double sum;

    /** For composite aggregations: the after_key for pagination */
    @JsonProperty("after_key")
    private JsonNode afterKey;

    public List<JsonNode> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<JsonNode> buckets) {
        this.buckets = buckets;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public SearchHits getHits() {
        return hits;
    }

    public void setHits(SearchHits hits) {
        this.hits = hits;
    }

    @JsonAnySetter
    public void setAggregation(String name, Object value) {
        if (value instanceof Map) {
            Aggregation agg = Aggregation.fromMap((Map<?, ?>) value);
            aggregations.put(name, agg);
        }
    }

    public static Aggregation fromMap(Map<?, ?> map) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(map, Aggregation.class);
    }

    public Map<String, Aggregation> getAggregations() {
        return aggregations;
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
    }

    public JsonNode getAfterKey() {
        return afterKey;
    }

    public void setAfterKey(JsonNode afterKey) {
        this.afterKey = afterKey;
    }
}
