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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a client-supplied {@code Idempotency-Key} first created, so a repeated
 * create carrying the same key can return the original owner instead of inserting a duplicate.
 * Keys are optional: a {@code null} or blank key is never remembered and never matches.
 */
@Component
public class IdempotencyKeyRegistry {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Return the id of the owner previously created under {@code key}, or {@code null} if the key is
     * absent, blank, or has not been seen before.
     */
    public Integer findOwnerId(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return this.ownerIdByKey.get(key);
    }

    /**
     * Associate {@code key} with the owner just created under it. No-op for a {@code null} or blank
     * key so unkeyed creates never occupy the registry.
     */
    public void remember(String key, Integer ownerId) {
        if (key != null && !key.isBlank()) {
            this.ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
