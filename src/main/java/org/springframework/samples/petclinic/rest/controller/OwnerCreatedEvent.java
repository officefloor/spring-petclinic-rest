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
 * Immutable structured audit event recorded when an owner is created. Serialized to JSON it
 * yields {@code {seq, ownerId, customerCode, membershipLevel, event:"OWNER_CREATED"}}.
 *
 * <p>The event carries the owner's <em>current primary identifier</em> under
 * {@code customerCode}. That identifier is resolved in a single place
 * ({@link #primaryIdentifier(Owner)}); when the primary identifier is later unified into the
 * memberId, changing that method is all that is needed for the event to carry the memberId.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the persisted owner's id
 * @param customerCode    the owner's current primary identifier
 * @param membershipLevel the owner's derived membership level
 * @param event           the event type discriminator, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
                                String event) {

    /** The event type discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /**
     * Builds an event for a freshly created owner.
     *
     * @param seq             the sequence number assigned to this create
     * @param owner           the persisted owner (id and primary identifier populated)
     * @param membershipLevel the owner's derived membership level
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, Integer membershipLevel) {
        return new OwnerCreatedEvent(seq, owner.getId(), primaryIdentifier(owner), membershipLevel, EVENT_TYPE);
    }

    /**
     * Resolves the owner's current primary identifier. This is the single seam through which
     * the primary identifier will later switch from the customer code to the memberId.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
