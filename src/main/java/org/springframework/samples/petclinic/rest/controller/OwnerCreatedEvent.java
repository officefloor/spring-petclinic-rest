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
 * Immutable structured record of a successful owner creation, serialized to the
 * {@code AUDIT} logger alongside the human-readable audit line.
 * <p>
 * The event carries the owner's id together with its
 * {@link Owner#getMemberId() member id}, the single value that identifies the owner,
 * so downstream consumers can follow the identifier. {@code seq} is a monotonically
 * increasing counter that orders creates.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String event) {

    private static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Captures {@code owner}'s state at creation under the given sequence number. Call
     * this only after the owner has been saved so its id and derived fields are set.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(), owner.getMembershipLevel(),
            OWNER_CREATED);
    }
}
