/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner that was created for a given {@code Idempotency-Key}, so a repeated
 * create carrying the same key can return the originally created owner instead of creating
 * a duplicate.
 *
 * <p>The mapping is kept in memory for the lifetime of the application; keys are supplied by
 * the client and are treated opaquely.
 */
@Component
public class IdempotencyKeyRegistry {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /**
     * Returns the id of the owner previously created for the given key, if any.
     *
     * @param key the idempotency key (may be {@code null} or blank, in which case nothing is found)
     * @return the previously created owner id, or empty if the key has not been seen
     */
    public Optional<Integer> findOwnerId(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.keyToOwnerId.get(key));
    }

    /**
     * Records that the given key produced the owner with the given id. No-op for a blank key.
     *
     * @param key     the idempotency key supplied with the create request
     * @param ownerId the id of the owner that was created
     */
    public void remember(String key, Integer ownerId) {
        if (key == null || key.isBlank() || ownerId == null) {
            return;
        }
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
