/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.samples.petclinic.audit;

import org.springframework.samples.petclinic.util.IdentityVersion;

/**
 * Immutable structured audit event recording that an owner was created.
 *
 * <p>The {@code memberId} field carries the owner's <em>current primary
 * identifier</em>: their unified member id. The event is emitted at schema version 2
 * ({@link IdentityVersion#VERSION}), which adds the {@code schemaVersion} and {@code ownerSegment}
 * fields.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param schemaVersion   the audit schema version of this event
 * @param ownerId         the persisted owner's id
 * @param memberId        the owner's current primary identifier
 * @param membershipLevel the owner's membership level
 * @param ownerSegment    the owner's segment, recomputed from the version-2 owner
 * @param event           the event type discriminator, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
        int membershipLevel, String ownerSegment, String event) {

    /** Discriminator value carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel, String ownerSegment) {
        this(seq, IdentityVersion.VERSION, ownerId, memberId, membershipLevel, ownerSegment, EVENT_TYPE);
    }
}
