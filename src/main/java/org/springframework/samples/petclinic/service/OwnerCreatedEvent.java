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
package org.springframework.samples.petclinic.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Immutable structured audit event emitted on the dedicated {@code AUDIT} logger when an owner is
 * created, alongside the human-readable audit line. Serialized (via the application's shared Jackson
 * {@code ObjectMapper}) to a JSON object
 * {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel, ownerSegment, event:'OWNER_CREATED'}}
 * where {@code seq} is a monotonically increasing integer across creates.
 *
 * <p>The event is at schema version 2: it carries a fixed {@code schemaVersion} of 2 and the owner's
 * {@code ownerSegment} recomputed from the version-2 identity (see
 * {@link org.springframework.samples.petclinic.model.Owner#ownerSegment()}). The {@code memberId}
 * field carries the owner's <em>current primary identifier</em> (see
 * {@link org.springframework.samples.petclinic.model.Owner#primaryIdentifier()}): the unified
 * version-2 {@code memberId}, so the event always names the owner by its live identity.
 *
 * @param seq a monotonically increasing sequence number across creates
 * @param ownerId the new owner's id
 * @param memberId the owner's current primary identifier
 * @param membershipLevel the owner's effective membership level
 * @param ownerSegment the owner's marketing segment recomputed from the version-2 identity
 */
@JsonPropertyOrder({"schemaVersion", "seq", "ownerId", "memberId", "membershipLevel", "ownerSegment",
    "event"})
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String ownerSegment) {

    /** The fixed {@code event} discriminator carried by every instance. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit event schema version carried by every instance. */
    public static final int SCHEMA_VERSION = 2;

    /** The schema version, serialized as the {@code schemaVersion} JSON property. */
    @JsonProperty("schemaVersion")
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    /** The event discriminator, serialized as the {@code event} JSON property. */
    @JsonProperty("event")
    public String event() {
        return EVENT;
    }
}
