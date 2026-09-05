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

import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable structured audit event emitted when an owner is created. It is serialized to a compact
 * JSON object {@code {seq, ownerId, memberId, membershipLevel, event}} and published on the
 * dedicated {@code AUDIT} logger alongside the human-readable audit line.
 *
 * <p>{@code seq} is a monotonically increasing sequence number across creates (assigned by the
 * caller), and {@code memberId} carries the owner's <em>current primary identifier</em>, the
 * unified {@link Owner#getMemberId() member id}. {@link #of(long, Owner)} is the single place it is
 * read, so a future change to which identifier is audited is confined there.
 *
 * <p>Being a {@code record} the event is immutable: once built it cannot be altered before it is
 * logged, which is the property an audit trail requires. The declaration order of the components is
 * the JSON field order.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    /** The fixed event type carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /** Shared, thread-safe mapper for rendering the event as a JSON string. */
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * Build the event for a freshly persisted {@code owner}, stamping it with the given sequence
     * number. The event's primary identifier is read here from the owner's current primary
     * identifier (the member id), so this factory is the one place to update when that identifier
     * changes.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(),
            owner.getMembershipLevel(), EVENT_TYPE);
    }

    /** This event rendered as a compact JSON object for the audit log. */
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
