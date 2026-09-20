/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for each client-supplied {@code Idempotency-Key}, so a create
 * that repeats with an already-seen key can return the originally created owner instead of creating
 * a duplicate. Keys are held in memory and mapped to the id of the owner first created under them.
 */
@Component
public class OwnerIdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * @return the id of the owner previously created under {@code key}, or empty if the key is
     * blank or has not been seen before.
     */
    public Optional<Integer> find(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /**
     * Records that {@code ownerId} was created under {@code key}. Blank keys are ignored. The first
     * owner recorded for a key wins; later creates with the same key resolve to it via {@link #find}.
     */
    public void record(String key, Integer ownerId) {
        if (key == null || key.isBlank()) {
            return;
        }
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
