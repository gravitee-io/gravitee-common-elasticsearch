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
package io.gravitee.elasticsearch.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

class ClientTest {

    /** A client implemented outside this library, without field capabilities. */
    private final Client clientWithoutFieldCapabilities = (Client) Proxy.newProxyInstance(
        Client.class.getClassLoader(),
        new Class<?>[] { Client.class },
        (proxy, method, args) -> {
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            throw new AssertionError("Unexpected call to " + method.getName());
        }
    );

    @Test
    void should_fail_field_capabilities_on_a_client_that_does_not_implement_them() throws InterruptedException {
        clientWithoutFieldCapabilities
            .getFieldCapabilities("gravitee-*", "additional-metrics.*")
            .test()
            .await()
            .assertError(
                error ->
                    error instanceof UnsupportedOperationException &&
                    "field capabilities are not supported by this client".equals(error.getMessage())
            );
    }
}
