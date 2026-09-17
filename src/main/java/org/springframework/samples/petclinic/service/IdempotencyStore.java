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
 * Remembers which owner a given {@code Idempotency-Key} first created, so a repeated create
 * carrying an already-seen key can replay the original result instead of creating a duplicate.
 *
 * <p>The mapping is key &rarr; created owner id. A blank or absent key is never stored and never
 * matches, so requests without a key follow the ordinary create path.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Look up the owner previously created under {@code key}.
     *
     * @param key the request's {@code Idempotency-Key} (may be {@code null} or blank)
     * @return the id of the originally created owner, or empty if the key is blank or unseen
     */
    public Optional<Integer> find(String key) {
        if (isBlank(key)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.ownerIdByKey.get(key));
    }

    /**
     * Remember that {@code key} first created the owner with the given id. Blank keys are ignored
     * and an already-seen key keeps its original id, so the first create always wins.
     *
     * @param key     the request's {@code Idempotency-Key} (may be {@code null} or blank)
     * @param ownerId the id of the owner created for this key
     */
    public void record(String key, Integer ownerId) {
        if (isBlank(key)) {
            return;
        }
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }

    private boolean isBlank(String key) {
        return key == null || key.isBlank();
    }
}
