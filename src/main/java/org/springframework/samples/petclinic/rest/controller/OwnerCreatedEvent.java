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
 * <p>Serializes to the JSON object {@code {seq, ownerId, customerCode, membershipLevel, event}}.
 * The {@code customerCode} carries the owner's current {@link Owner#getPrimaryIdentifier() primary
 * identifier}, so it tracks that identifier automatically if the customer code is later unified into
 * the membership number.
 *
 * @param seq            monotonically increasing sequence number across owner creations
 * @param ownerId        the created owner's id
 * @param customerCode   the owner's current primary identifier
 * @param membershipLevel the owner's membership level at creation
 * @param event          the event marker, always {@value #EVENT}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
                                String event) {

    /** The event marker distinguishing this event type on the {@code AUDIT} stream. */
    public static final String EVENT = "OWNER_CREATED";

    /**
     * Build the event for the given persisted owner and sequence number, reading the owner's current
     * primary identifier so the event stays correct as that identifier evolves.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getPrimaryIdentifier(),
            owner.getMembershipLevel(), EVENT);
    }
}
