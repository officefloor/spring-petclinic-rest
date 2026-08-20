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

package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted (as JSON, via the {@code AUDIT} logger) whenever an owner
 * is created, alongside the human-readable audit line. This is the version-2 schema: its fields
 * serialize in declaration order to
 * {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment, event}}.
 *
 * <p>{@code schemaVersion} is a fixed integer identifying the audit schema (always {@code 2}). {@code
 * seq} is a monotonically increasing sequence number assigned across all creates, letting a consumer
 * order events and detect gaps. The event carries the owner's <em>current primary identifier</em>, the
 * unified version-2 {@code memberId} (see {@link #forCreatedOwner}), so downstream consumers always
 * read the same field to obtain whatever the owner's primary identifier currently is, plus the owner's
 * segment recomputed from the version-2 identity.
 *
 * <p>Being a record, the event is immutable once constructed: a captured event cannot be altered
 * after the fact.
 *
 * @param schemaVersion the audit schema version, always {@link #SCHEMA_VERSION}
 * @param seq the monotonically increasing sequence number of this create
 * @param ownerId the created owner's id
 * @param memberId the owner's current primary identifier (the unified version-2 member id)
 * @param membershipLevel the owner's membership level as returned to the client
 * @param ownerSegment the owner's segment recomputed from the version-2 identity
 * @param event the event marker, always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
                                Integer membershipLevel, String ownerSegment, String event) {

    /** The event marker identifying an owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The version-2 audit schema version stamped on every event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Builds the version-2 owner-created event for a freshly persisted owner, reading the owner's
     * current primary identifier ({@link Owner#getMemberId() memberId}) so the event always carries
     * whatever identifier the owner is currently keyed by, and stamping the fixed
     * {@link #SCHEMA_VERSION}.
     *
     * @param seq the monotonically increasing sequence number to stamp on the event
     * @param owner the created owner, with its id and primary identifier populated
     * @param membershipLevel the owner's membership level as returned to the client
     * @param ownerSegment the owner's segment recomputed from the version-2 identity
     * @return the immutable owner-created event
     */
    public static OwnerCreatedEvent forCreatedOwner(long seq, Owner owner, Integer membershipLevel,
                                                    String ownerSegment) {
        return new OwnerCreatedEvent(SCHEMA_VERSION, seq, owner.getId(), owner.getMemberId(),
            membershipLevel, ownerSegment, OWNER_CREATED);
    }
}
