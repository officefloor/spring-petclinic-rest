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
 * Immutable structured audit event emitted, alongside the human-readable audit line, when an owner
 * is created. Serialized to the {@code AUDIT} logger as a JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event}}.
 *
 * <p>{@code seq} is a monotonically increasing sequence number assigned across owner creates, so the
 * ordering of creates can be reconstructed from the audit stream. {@code memberId} carries the owner's
 * primary identifier, its unified member id - populated from that single primary-identifier source in
 * the controller so the audit's identifier has one home.
 *
 * <p>Being a record, the event is immutable once constructed: an emitted event can never be mutated.
 *
 * @param seq the monotonically increasing create sequence number
 * @param ownerId the created owner's id
 * @param memberId the owner's primary identifier (its unified member id)
 * @param membershipLevel the owner's reported membership level
 * @param event the event type discriminator, always {@code "OWNER_CREATED"}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
                               String event) {

    /** The event-type discriminator carried by every owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, OWNER_CREATED);
    }
}
