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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Reads {@code GET /} responses as returned by Elasticsearch and OpenSearch.
 *
 * @author GraviteeSource Team
 */
class ElasticsearchInfoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class BuildFlavor {

        @Test
        void is_read_from_the_version() throws JsonProcessingException {
            ElasticsearchInfo info = read(
                """
                {
                  "name": "node-1",
                  "cluster_name": "gravitee",
                  "cluster_uuid": "uuid",
                  "version": {
                    "number": "8.17.2",
                    "build_flavor": "default",
                    "build_type": "docker",
                    "build_snapshot": false,
                    "lucene_version": "9.12.0",
                    "minimum_wire_compatibility_version": "7.17.0",
                    "minimum_index_compatibility_version": "7.0.0"
                  },
                  "tagline": "You Know, for Search"
                }
                """
            );

            assertThat(info.getBuildFlavor()).contains("default");
            assertThat(info.getVersion().getBuildFlavor()).isEqualTo("default");
        }

        @Test
        void is_serverless_on_elastic_cloud_serverless() throws JsonProcessingException {
            ElasticsearchInfo info = read(
                """
                {
                  "name": "serverless",
                  "cluster_name": "project-id",
                  "cluster_uuid": "uuid",
                  "version": {
                    "number": "8.11.0",
                    "build_flavor": "serverless",
                    "build_type": "docker",
                    "build_hash": "00000000",
                    "build_date": "2023-10-31",
                    "build_snapshot": false,
                    "lucene_version": "9.7.0",
                    "minimum_wire_compatibility_version": "8.11.0",
                    "minimum_index_compatibility_version": "8.11.0"
                  },
                  "tagline": "You Know, for Search"
                }
                """
            );

            assertThat(info.getBuildFlavor()).contains("serverless");
            assertThat(info.getVersion().getMajorVersion()).isEqualTo(8);
            assertThat(info.getVersion().isOpenSearch()).isFalse();
        }

        @Test
        void is_absent_when_the_response_does_not_carry_one() throws JsonProcessingException {
            ElasticsearchInfo info = read(
                """
                {
                  "name": "node-1",
                  "cluster_name": "gravitee",
                  "cluster_uuid": "uuid",
                  "version": {
                    "distribution": "opensearch",
                    "number": "2.19.1",
                    "build_type": "tar",
                    "build_snapshot": false,
                    "lucene_version": "9.12.1",
                    "minimum_wire_compatibility_version": "7.10.0",
                    "minimum_index_compatibility_version": "7.0.0"
                  },
                  "tagline": "The OpenSearch Project: https://opensearch.org/"
                }
                """
            );

            assertThat(info.getBuildFlavor()).isEmpty();
            assertThat(info.getVersion().getBuildFlavor()).isNull();
        }

        @Test
        void is_absent_when_the_response_has_no_version() throws JsonProcessingException {
            ElasticsearchInfo info = read("{ \"error\": { \"type\": \"security_exception\" }, \"status\": 401 }");

            assertThat(info.getBuildFlavor()).isEmpty();
        }

        @Test
        void is_not_serialized_twice() throws JsonProcessingException {
            ElasticsearchInfo info = read("{ \"version\": { \"number\": \"8.11.0\", \"build_flavor\": \"serverless\" } }");

            String json = mapper.writeValueAsString(info);

            assertThat(json).contains("\"build_flavor\":\"serverless\"").doesNotContain("buildFlavor");
        }
    }

    @Nested
    class MajorVersion {

        @ParameterizedTest(name = "{0} {1} -> {2}")
        @CsvSource(
            {
                "elasticsearch, 7.17.28, 7",
                "elasticsearch, 8.17.2, 8",
                "elasticsearch, 8.11.0-SNAPSHOT, 8",
                "elasticsearch, 9.1.0, 9",
                "elasticsearch, 10.0.0, 10",
                "elasticsearch, 10.0.0-beta1, 10",
                "opensearch, 1.3.20, 1",
                "opensearch, 2.19.1, 2",
                "opensearch, 3.0.0, 3",
            }
        )
        void is_the_full_numeric_segment_before_the_first_dot(String distribution, String number, int expectedMajor)
            throws JsonProcessingException {
            String distributionField = "opensearch".equals(distribution) ? "\"distribution\": \"opensearch\", " : "";
            ElasticsearchInfo info = read("{ \"version\": { " + distributionField + "\"number\": \"" + number + "\" } }");

            assertThat(info.getVersion().getMajorVersion()).isEqualTo(expectedMajor);
            assertThat(info.getVersion().isOpenSearch()).isEqualTo("opensearch".equals(distribution));
        }

        @Test
        void keeps_elasticsearch_10_on_the_recent_code_paths() throws JsonProcessingException {
            Version version = read("{ \"version\": { \"number\": \"10.0.0\" } }").getVersion();

            assertThat(version.canUseTypeRequests()).isFalse();
            assertThat(version.canUseMultiTypeIndex()).isFalse();
            assertThat(version.canUseIlmManagedIndex()).isTrue();
            assertThat(version.canUseDateHistogramFixedInterval()).isTrue();
        }
    }

    private ElasticsearchInfo read(String json) throws JsonProcessingException {
        return mapper.readValue(json, ElasticsearchInfo.class);
    }
}
