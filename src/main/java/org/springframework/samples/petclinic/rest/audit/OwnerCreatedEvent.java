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

package org.springframework.samples.petclinic.rest.audit;

/**
 * Immutable structured record of a successful owner creation, serialized as a JSON object to the
 * {@code AUDIT} trail alongside the human-readable audit line. The declaration order of the
 * components mirrors the emitted JSON: {@code {seq, ownerId, memberId, membershipLevel, event}}.
 *
 * <p>The {@code memberId} component carries the owner's primary identifier at creation time
 * (see {@link org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}).
 *
 * @param seq             monotonically increasing sequence number across all owner creations
 * @param ownerId         the persisted owner's id
 * @param memberId        the owner's primary identifier at creation time
 * @param membershipLevel the owner's membership level at creation time
 * @param event           the event-type marker, always {@link #TYPE}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel, String event) {

    /** The event-type marker every owner-creation event carries. */
    public static final String TYPE = "OWNER_CREATED";
}
