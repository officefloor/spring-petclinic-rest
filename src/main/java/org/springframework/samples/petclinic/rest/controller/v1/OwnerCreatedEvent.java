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

/**
 * Immutable structured audit event emitted once per successful owner create, in addition to the
 * human-readable audit line. Serialized to JSON on the dedicated {@code AUDIT} logger.
 *
 * <p>The {@code seq} is a monotonically increasing sequence across all creates. The
 * {@code memberId} carries the owner's <em>current primary identifier</em>; today that is the
 * owner's unified member id.
 *
 * <p>Being a record, every field is final and the instance is immutable once constructed.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the newly created owner's id
 * @param memberId        the owner's current primary identifier (the member id)
 * @param membershipLevel the owner's derived membership level
 * @param event           the event marker, always {@code OWNER_CREATED}
 */
public record OwnerCreatedEvent(int seq, Integer ownerId, String memberId, Integer membershipLevel,
                                String event) {

    /** The fixed event marker for an owner-create event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Builds an {@code OWNER_CREATED} event carrying the given sequence number, owner id, primary
     * identifier and membership level.
     *
     * @param seq               monotonically increasing sequence number across creates
     * @param ownerId           the newly created owner's id
     * @param primaryIdentifier the owner's current primary identifier
     * @param membershipLevel   the owner's derived membership level
     * @return the immutable event
     */
    public static OwnerCreatedEvent of(int seq, Integer ownerId, String primaryIdentifier, Integer membershipLevel) {
        return new OwnerCreatedEvent(seq, ownerId, primaryIdentifier, membershipLevel, OWNER_CREATED);
    }
}
