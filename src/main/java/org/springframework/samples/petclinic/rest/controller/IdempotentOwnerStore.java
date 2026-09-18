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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for a given client-supplied
 * {@code Idempotency-Key}.
 * <p>
 * This lets the create endpoint be idempotent: when a create repeats with a key
 * that has already been seen, the originally created owner is returned instead
 * of creating a duplicate. Only the owner's id is retained; the owner itself is
 * re-read from the store so the returned state always reflects what is persisted.
 */
@Component
public class IdempotentOwnerStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * The id of the owner previously created under {@code key}, or an empty
     * {@link Optional} if the key has not been seen.
     *
     * @param key the client-supplied idempotency key
     */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /**
     * Records that {@code owner} was created under {@code key}. Call once the owner
     * has been saved and has an id.
     *
     * @param key   the client-supplied idempotency key
     * @param owner the owner just created
     */
    public void remember(String key, Owner owner) {
        ownerIdByKey.put(key, owner.getId());
    }
}
