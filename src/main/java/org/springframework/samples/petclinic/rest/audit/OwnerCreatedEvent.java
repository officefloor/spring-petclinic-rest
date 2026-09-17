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

package org.springframework.samples.petclinic.rest.audit;

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured record of an owner having been created, emitted to the {@code AUDIT} logger
 * alongside the human-readable audit line.
 *
 * <p>The {@code memberId} slot carries the owner's {@link Owner#getPrimaryIdentifier() current
 * primary identifier} rather than the member id field directly, so that when a different identifier
 * later becomes primary the event follows automatically without touching this type.
 *
 * <p>This is schema version 2 of the event: it carries the {@link #schemaVersion} explicitly and the
 * owner segment recomputed from the version-2 identity. The {@code schemaVersion} tracks the shared
 * {@link IdentityVersion#VERSION identity version}.
 *
 * @param seq            monotonically increasing sequence number across creates
 * @param ownerId        the persisted owner's id
 * @param memberId       the owner's current primary identifier
 * @param membershipLevel the owner's derived membership level
 * @param ownerSegment   the owner's segment, recomputed from the version-2 identity
 * @param schemaVersion  the audit event schema version, always {@link IdentityVersion#VERSION}
 * @param event          the event marker, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
                                String ownerSegment, int schemaVersion, String event) {

    /** The marker identifying this event kind. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /**
     * Build the event for a freshly persisted owner, reading its current primary identifier so the
     * event stays correct as the primary identifier evolves and recomputing its owner segment from
     * the version-2 identity.
     *
     * @param seq   the sequence number to assign to this event
     * @param owner the owner that has just been persisted
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getPrimaryIdentifier(),
            owner.getMembershipLevel(), owner.getOwnerSegment(), IdentityVersion.VERSION, EVENT_TYPE);
    }
}
