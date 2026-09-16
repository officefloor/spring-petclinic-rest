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

import org.springframework.samples.petclinic.model.IdentityVersion;

/**
 * Immutable structured audit event recording the creation of an owner. It captures the
 * create's monotonic {@code seq}, the owner's id, its current primary identifier (the
 * version-2 {@code memberId}), its membership level and its recomputed owner segment, and
 * is serialized to JSON on the {@code AUDIT} trail alongside the human-readable audit line.
 *
 * <p>This is schema version 2 of the event: it carries a {@code schemaVersion} of
 * {@value org.springframework.samples.petclinic.model.IdentityVersion#NUMBER} and the owner
 * segment recomputed from the version-2 identity.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String ownerSegment, int schemaVersion, String event) {

    /** The fixed discriminator identifying this kind of audit event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
            String ownerSegment) {
        this(seq, ownerId, memberId, membershipLevel, ownerSegment, IdentityVersion.NUMBER, EVENT_TYPE);
    }
}
