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

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public enum Type {
    REQUEST("request", false),
    HEALTH_CHECK("health", false),
    LOG("log", false),
    MONITOR("monitor", false),
    V4_LOG("v4-log", false),
    V4_METRICS("v4-metrics", false),
    V4_MESSAGE_METRICS("v4-message-metrics", false),
    V4_MESSAGE_LOG("v4-message-log", false),
    EVENT_METRICS("event-metrics", true);

    private final String type;

    public static final Type[] TYPES = new Type[] {
        REQUEST,
        MONITOR,
        HEALTH_CHECK,
        LOG,
        V4_LOG,
        V4_METRICS,
        V4_MESSAGE_LOG,
        V4_MESSAGE_METRICS,
        EVENT_METRICS,
    };
    private final boolean dataStream;

    Type(final String type, boolean dataStream) {
        this.type = type;
        this.dataStream = dataStream;
    }

    public String getType() {
        return type;
    }

    public boolean isDataStream() {
        return dataStream;
    }
}
