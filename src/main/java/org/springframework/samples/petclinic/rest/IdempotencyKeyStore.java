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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each seen {@code Idempotency-Key}, so that a create request
 * repeated with an already-seen key returns the originally created owner instead of creating a
 * duplicate. The store maps an idempotency key to the id of the owner it first created; a repeat of
 * that key is served from the recorded id rather than re-running the create flow.
 *
 * <p>Keys are held in a process-wide, thread-safe map: the mapping must outlive the single request
 * that established it so the next request carrying the same key can see it. Recording a key is
 * idempotent - the first owner id recorded for a key is kept, so concurrent repeats all resolve to
 * the same original owner.
 */
@Component
public class IdempotencyKeyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Returns the id of the owner previously created for the given idempotency key, or empty when the
     * key has not been seen (or is {@code null}/blank, i.e. no key was supplied).
     *
     * @param key the request's {@code Idempotency-Key}, or {@code null}/blank when none was supplied
     * @return the recorded owner id for the key, or empty when the key is unknown or absent
     */
    public Optional<Integer> find(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ownerIdByKey.get(key));
    }

    /**
     * Records that the given idempotency key first created the owner with {@code ownerId}, keeping the
     * first id recorded for a key on repeated calls. A {@code null}/blank key is ignored (nothing to
     * remember when no key was supplied).
     *
     * @param key the request's {@code Idempotency-Key}, or {@code null}/blank when none was supplied
     * @param ownerId the id of the owner created for that key
     */
    public void record(String key, Integer ownerId) {
        if (key == null || key.isBlank() || ownerId == null) {
            return;
        }
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
