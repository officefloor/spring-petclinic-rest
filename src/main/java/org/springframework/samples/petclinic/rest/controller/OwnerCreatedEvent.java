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

import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSegment;

/**
 * Immutable structured audit event recorded when an owner is created. Serialized to JSON it
 * yields {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment,
 * event:"OWNER_CREATED"}}.
 *
 * <p>The event carries the owner's <em>current primary identifier</em> under
 * {@code memberId}. That identifier is resolved in a single place
 * ({@link #primaryIdentifier(Owner)}). The {@code ownerSegment} is recomputed for the version-2
 * identity from the owner's plain region and membership level, matching the segment returned by
 * the owner response.
 *
 * @param schemaVersion   the audit-event schema version, always {@link #SCHEMA_VERSION}
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the persisted owner's id
 * @param memberId        the owner's current primary identifier
 * @param membershipLevel the owner's derived membership level
 * @param ownerSegment    the owner's segment, {@code "<TIER>_<AREA>"}
 * @param event           the event type discriminator, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
                                Integer membershipLevel, String ownerSegment, String event) {

    /** The event type discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /** The audit-event schema version carried by every owner-created event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Builds an event for a freshly created owner.
     *
     * @param seq             the sequence number assigned to this create
     * @param owner           the persisted owner (id and primary identifier populated)
     * @param membershipLevel the owner's derived membership level
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, Integer membershipLevel) {
        String ownerSegment = OwnerSegment.of(membershipLevel, Locality.forOwner(owner));
        return new OwnerCreatedEvent(SCHEMA_VERSION, seq, owner.getId(), primaryIdentifier(owner),
            membershipLevel, ownerSegment, EVENT_TYPE);
    }

    /**
     * Resolves the owner's current primary identifier: the unified {@code memberId}.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
