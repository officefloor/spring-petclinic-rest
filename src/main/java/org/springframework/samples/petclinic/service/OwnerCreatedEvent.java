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
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}} where {@code seq} is a
 * monotonically increasing integer across creates.
 *
 * <p>The {@code customerCode} field carries the owner's <em>current primary identifier</em> (see
 * {@link org.springframework.samples.petclinic.model.Owner#primaryIdentifier()}): the customer code
 * today, and whatever replaces it later (e.g. the unified {@code memberId}), so the event always names
 * the owner by its live identity without this type changing.
 *
 * @param seq a monotonically increasing sequence number across creates
 * @param ownerId the new owner's id
 * @param customerCode the owner's current primary identifier
 * @param membershipLevel the owner's effective membership level
 */
@JsonPropertyOrder({"seq", "ownerId", "customerCode", "membershipLevel", "event"})
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {

    /** The fixed {@code event} discriminator carried by every instance. */
    public static final String EVENT = "OWNER_CREATED";

    /** The event discriminator, serialized as the {@code event} JSON property. */
    @JsonProperty("event")
    public String event() {
        return EVENT;
    }
}
