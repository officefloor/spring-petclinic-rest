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

/**
 * Immutable structured audit event recording the creation of an owner. It captures the
 * create's monotonic {@code seq}, the owner's id, its current primary identifier (the
 * {@code customerCode}, and whatever later replaces it) and its membership level, and is
 * serialized to JSON on the {@code AUDIT} trail alongside the human-readable audit line.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** The fixed discriminator identifying this kind of audit event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, EVENT_TYPE);
    }
}
