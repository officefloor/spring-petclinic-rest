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
package org.springframework.samples.petclinic.rest;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevelCalculator;

/**
 * Immutable structured audit event recording that an owner was created. Serialized to the
 * {@code AUDIT} logger as a JSON object {@code {seq, ownerId, customerCode, membershipLevel,
 * event}}.
 *
 * <p>The {@code customerCode} field carries the owner's current
 * {@link Owner#primaryIdentifier() primary identifier}, which is the customer code today; when the
 * customer code is unified into a member id the same field will carry the member id instead, so
 * downstream consumers keep receiving whatever presently identifies the owner.
 *
 * @param seq             monotonically increasing sequence number, unique per created owner
 * @param ownerId         the persisted owner's id
 * @param customerCode    the owner's current primary identifier
 * @param membershipLevel the owner's effective membership level at creation
 * @param event           the event marker, always {@link #EVENT_NAME}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
                                String event) {

    /** Event marker distinguishing this event from other audit events on the {@code AUDIT} logger. */
    public static final String EVENT_NAME = "OWNER_CREATED";

    /**
     * Build the event for a freshly persisted owner, stamping it with the given sequence number and
     * capturing the owner's current primary identifier and effective membership level.
     *
     * @param seq   the sequence number to assign
     * @param owner the persisted owner
     */
    public OwnerCreatedEvent(long seq, Owner owner) {
        this(seq, owner.getId(), owner.primaryIdentifier(),
            MembershipLevelCalculator.effectiveMembershipLevel(owner), EVENT_NAME);
    }
}
