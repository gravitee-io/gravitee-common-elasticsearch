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

    /** If the aggregation is a metric one */
    private Float value;

    /** If the aggregation is a percentile */
    private Map<String, Float> values;

    /** If the aggregation is a stats one */
    private Float count;

    /** If the aggregation is a stats one */
    private Float min;

    /** If the aggregation is a stats one */
    private Float max;

    /** If the aggregation is a stats one */
    private Float avg;

    /** If the aggregation is a stats one */
    private Float sum;

    public List<JsonNode> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<JsonNode> buckets) {
        this.buckets = buckets;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public Float getCount() {
        return count;
    }

    public void setCount(Float count) {
        this.count = count;
    }

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    public Float getAvg() {
        return avg;
    }

    public void setAvg(Float avg) {
        this.avg = avg;
    }

    public Float getSum() {
        return sum;
    }

    public void setSum(Float sum) {
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

    public Map<String, Float> getValues() {
        return values;
    }

    public void setValues(Map<String, Float> values) {
        this.values = values;
    }
}
