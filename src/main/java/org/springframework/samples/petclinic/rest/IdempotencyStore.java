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

package org.springframework.samples.petclinic.rest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for a given {@code Idempotency-Key}, so that a create
 * request that repeats with an already-seen key can return the originally created owner
 * instead of creating a duplicate. Keys are only recorded once the original create has
 * succeeded, so a failed attempt does not "burn" a key.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdsByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created for {@code key}, if any. */
    public Optional<Integer> find(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ownerIdsByKey.get(key));
    }

    /** Record the owner created for {@code key}. No-op when the key is blank. */
    public void remember(String key, Integer ownerId) {
        if (key != null && !key.isBlank() && ownerId != null) {
            ownerIdsByKey.putIfAbsent(key, ownerId);
        }
    }
}
