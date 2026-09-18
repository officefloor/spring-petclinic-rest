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
package org.springframework.samples.petclinic.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given client-supplied {@code Idempotency-Key} first created,
 * so a repeated create carrying an already-seen key can return the originally created
 * owner instead of persisting a duplicate.
 */
@Component
public class IdempotencyKeyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Return the id of the owner previously created under {@code key}, if any.
     *
     * @param key the client-supplied idempotency key
     * @return the originally created owner id, or empty if the key has not been seen
     */
    public Optional<Integer> findOwnerId(String key) {
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /**
     * Associate {@code key} with the owner that was created for it. The first association
     * wins, so concurrent repeats never overwrite the original owner.
     *
     * @param key     the client-supplied idempotency key
     * @param ownerId the id of the owner created under that key
     */
    public void remember(String key, Integer ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
