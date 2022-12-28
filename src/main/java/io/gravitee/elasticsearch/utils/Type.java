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
package io.gravitee.elasticsearch.utils;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public enum Type {
    REQUEST("request"),
    HEALTH_CHECK("health"),
    LOG("log"),
    MONITOR("monitor"),
    LOG_V4("log-v4"),
    METRICS_V4("metrics-v4"),
    MESSAGE_METRICS_V4("message-metrics-v4"),
    MESSAGE_LOG_V4("message-log-v4"),

    // For ES7 support
    DOC("_doc");

    private final String type;

    public static final Type[] TYPES = new Type[] {
        REQUEST,
        MONITOR,
        HEALTH_CHECK,
        LOG,
        LOG_V4,
        METRICS_V4,
        MESSAGE_LOG_V4,
        MESSAGE_METRICS_V4,
    };

    Type(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
