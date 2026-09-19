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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event recording that an owner was created.
 *
 * <p>Serializes to the schema-version-2 JSON object
 * {@code {seq, schemaVersion, ownerId, memberId, membershipLevel, ownerSegment, event}}. The
 * {@code memberId} carries the owner's version-2 {@link Owner#getMemberId() member id}, the single
 * identifier for the owner, and {@code ownerSegment} is recomputed from the version-2 identity.
 *
 * @param seq            monotonically increasing sequence number across owner creations
 * @param schemaVersion  the audit event schema version, always {@value #SCHEMA_VERSION}
 * @param ownerId        the created owner's id
 * @param memberId       the owner's member id
 * @param membershipLevel the owner's membership level at creation
 * @param ownerSegment   the owner's segment recomputed from the version-2 identity
 * @param event          the event marker, always {@value #EVENT}
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
                                Integer membershipLevel, String ownerSegment, String event) {

    /** The event marker distinguishing this event type on the {@code AUDIT} stream. */
    public static final String EVENT = "OWNER_CREATED";

    /** The schema version of this structured audit event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Build the event for the given persisted owner and sequence number.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, SCHEMA_VERSION, owner.getId(), owner.getMemberId(),
            owner.getMembershipLevel(), owner.getOwnerSegment(), EVENT);
    }
}
