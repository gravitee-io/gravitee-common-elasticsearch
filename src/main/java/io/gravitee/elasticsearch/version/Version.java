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
package io.gravitee.elasticsearch.version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Version {

    private static final String OPENSEARCH_DISTRIBUTION = "opensearch";

    private String number;

    @JsonProperty("lucene_version")
    private String luceneVersion;

    private String distribution;

    /**
     * Build flavor reported by Elasticsearch ({@code default}, {@code oss}, {@code serverless}, ...).
     * Absent from some responses, OpenSearch in particular.
     */
    @JsonProperty("build_flavor")
    private String buildFlavor;

    private int majorVersion = -1;

    private int minorVersion = -1;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLuceneVersion() {
        return luceneVersion;
    }

    public void setLuceneVersion(String luceneVersion) {
        this.luceneVersion = luceneVersion;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    /**
     * @return the build flavor reported by Elasticsearch, or {@code null} when the response does not carry one
     */
    public String getBuildFlavor() {
        return buildFlavor;
    }

    public void setBuildFlavor(String buildFlavor) {
        this.buildFlavor = buildFlavor;
    }

    public int getMajorVersion() {
        if (majorVersion == -1) {
            majorVersion = parseLeadingNumber(getNumber());
        }
        return majorVersion;
    }

    /**
     * @return the numeric segment after the first dot: {@code 8.14.0} gives 14, {@code 10.0.0-beta1} gives 0
     * @throws IllegalStateException when the version number has no minor segment
     */
    public int getMinorVersion() {
        if (minorVersion == -1) {
            final String number = getNumber();
            final int dot = number.indexOf('.');
            if (dot == -1) {
                throw new IllegalStateException("Elasticsearch version number has no minor segment: " + number);
            }
            minorVersion = parseLeadingNumber(number.substring(dot + 1));
        }
        return minorVersion;
    }

    /**
     * Read the leading digits of a version segment: {@code 10.0.0} gives 10, {@code 8.11.0-SNAPSHOT} gives 8.
     */
    private static int parseLeadingNumber(final String segment) {
        int end = 0;
        while (end < segment.length() && Character.isDigit(segment.charAt(end))) {
            end++;
        }
        return Integer.parseInt(segment.substring(0, end));
    }

    public boolean isOpenSearch() {
        return OPENSEARCH_DISTRIBUTION.equals(distribution);
    }

    public boolean canUseTypeRequests() {
        // from ES version 7, specifying types in requests is deprecated
        return !isOpenSearch() && getMajorVersion() < 7;
    }

    public boolean canUseMultiTypeIndex() {
        // from ES version 6, using multiple mapping types is no more supported
        return !isOpenSearch() && getMajorVersion() < 6;
    }

    public boolean canUseIlmManagedIndex() {
        // from ES version 6, we can use ILM managed indexes, which names are not suffixed by date
        return isOpenSearch() || getMajorVersion() >= 6;
    }

    public boolean canUseDateHistogramFixedInterval() {
        // in ES version 7, time interval in dateHistogram aggregation is deprecated and removed in ES 8. Instead, we have to use fixed_interval
        return !isOpenSearch() && getMajorVersion() >= 7;
    }

    public boolean canUseDataStreams() {
        // data streams came with Elasticsearch 7.9, and with every OpenSearch version
        return isOpenSearch() || isAtLeast(7, 9);
    }

    public boolean canUseDataStreamLifecycle() {
        // the data stream lifecycle (data_retention) is generally available from Elasticsearch 8.14; OpenSearch has none
        return !isOpenSearch() && isAtLeast(8, 14);
    }

    private boolean isAtLeast(final int major, final int minor) {
        return getMajorVersion() > major || (getMajorVersion() == major && getMinorVersion() >= minor);
    }
}
