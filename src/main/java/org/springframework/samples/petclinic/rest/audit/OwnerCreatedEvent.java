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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable structured audit event emitted on a successful owner create, alongside the
 * human-readable audit line. It is rendered to a stable JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event}} - the component declaration order is
 * the serialized field order - and published to the dedicated {@code AUDIT} logger.
 *
 * <p>{@code seq} is a monotonically increasing sequence number assigned across creates, so events
 * carry a total order independent of the owner id.
 *
 * <p>{@code memberId} carries the owner's primary identifier - the unified member id assigned at
 * creation.
 *
 * <p>The record is immutable: its components are set once at construction and never mutated, so a
 * published event is a faithful, tamper-proof snapshot of the create.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
                                String event) {

    /** The fixed {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * Creates an {@code OWNER_CREATED} event with the given sequence number, owner id, primary
     * identifier and membership level. The {@code event} discriminator is fixed to
     * {@link #EVENT_TYPE}.
     */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, EVENT_TYPE);
    }

    /**
     * Renders this event as a compact JSON object with fields in declaration order:
     * {@code {"seq":..,"ownerId":..,"memberId":..,"membershipLevel":..,"event":"OWNER_CREATED"}}.
     */
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
