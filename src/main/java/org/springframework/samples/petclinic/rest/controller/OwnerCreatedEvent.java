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

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured record of a successful owner creation, serialized to the
 * {@code AUDIT} logger alongside the human-readable audit line.
 * <p>
 * This is schema version 2 of the event, carried in {@code schemaVersion}: alongside the
 * owner's id and its {@link Owner#getMemberId() member id} (the single value that identifies
 * the owner) it carries the {@link Owner#getOwnerSegment() owner segment} recomputed from the
 * owner's version-2 identity, so downstream consumers can follow the identifier and segment.
 * {@code seq} is a monotonically increasing counter that orders creates.
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    private static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Captures {@code owner}'s state at creation under the given sequence number. Call
     * this only after the owner has been saved so its id and derived fields are set.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, IdentityVersion.NUMBER, owner.getId(), owner.getMemberId(),
            owner.getMembershipLevel(), owner.getOwnerSegment(), OWNER_CREATED);
    }
}
