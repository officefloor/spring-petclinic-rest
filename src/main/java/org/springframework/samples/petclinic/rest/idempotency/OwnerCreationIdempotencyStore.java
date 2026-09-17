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

package org.springframework.samples.petclinic.rest.idempotency;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for a given {@code Idempotency-Key}, so a create that
 * repeats with an already-seen key resolves to the originally created owner instead of
 * creating a duplicate.
 */
@Component
public class OwnerCreationIdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner originally created for {@code key}, if that key has been seen. */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(this.ownerIdByKey.get(key));
    }

    /** Record that {@code key} produced the owner with {@code ownerId}. */
    public void remember(String key, Integer ownerId) {
        this.ownerIdByKey.put(key, ownerId);
    }
}
