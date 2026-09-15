/*
 * Copyright 2002-2017 the original author or authors.
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
 * Remembers which owner a given {@code Idempotency-Key} first created, so a repeated create
 * carrying an already-seen key can replay the original owner instead of storing a duplicate.
 *
 * <p>Only the created owner's id is retained; the owner itself is re-read from the store on
 * replay so the returned representation reflects its current state.
 */
@Component
public class OwnerCreationIdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Looks up the owner previously created for an idempotency key.
     *
     * @param key the request's {@code Idempotency-Key}
     * @return the id of the owner first created with this key, or empty if the key is unseen
     */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /**
     * Records the owner created for an idempotency key. The first key wins: a later create
     * reusing the same key does not overwrite the original mapping.
     *
     * @param key     the request's {@code Idempotency-Key}
     * @param ownerId the id of the owner that was created
     */
    public void record(String key, Integer ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
