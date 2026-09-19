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

/**
 * Immutable structured audit event recording that an owner was created.
 *
 * <p>The {@code memberId} field carries the owner's <em>current primary
 * identifier</em>: their unified member id.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the persisted owner's id
 * @param memberId        the owner's current primary identifier
 * @param membershipLevel the owner's membership level
 * @param event           the event type discriminator, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel, String event) {

    /** Discriminator value carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, EVENT_TYPE);
    }
}
