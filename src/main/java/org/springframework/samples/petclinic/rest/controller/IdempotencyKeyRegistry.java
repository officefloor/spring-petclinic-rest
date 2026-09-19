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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given {@code Idempotency-Key} first created, so a create that repeats
 * with an already-seen key can return the original owner instead of creating a duplicate.
 *
 * <p>Blank or absent keys are treated as "no key": they are never stored and never match, so
 * callers can pass the raw header value through without null-checking it first.
 */
@Component
public class IdempotencyKeyRegistry {

    private final ConcurrentMap<String, Integer> ownerIdsByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created under {@code key}, if any. */
    public Optional<Integer> ownerIdFor(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ownerIdsByKey.get(key));
    }

    /** Associate {@code key} with the owner it created; a no-op for a blank or absent key. */
    public void remember(String key, Integer ownerId) {
        if (key != null && !key.isBlank()) {
            ownerIdsByKey.putIfAbsent(key, ownerId);
        }
    }
}
