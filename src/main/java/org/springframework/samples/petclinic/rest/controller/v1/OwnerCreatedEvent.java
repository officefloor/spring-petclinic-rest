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

import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable structured audit event published to the {@code AUDIT} logger when an owner is created,
 * alongside the human-readable audit line. It is serialized to a compact JSON object of the form
 * {@code {"seq":..,"ownerId":..,"memberId":..,"ownerSegment":..,"membershipLevel":..,
 * "schemaVersion":2,"event":"OWNER_CREATED"}}.
 *
 * <p>The {@code seq} is a monotonically increasing sequence number assigned across all owner
 * creates, giving the event stream a total order independent of timestamps.
 *
 * <p>The {@code memberId} component carries the owner's <em>current primary identifier</em>: the
 * unified (version-2) member id. The caller supplies whatever the owner's current primary identifier
 * is, so this event always reflects it. The {@code ownerSegment} is the owner's marketing segment
 * recomputed from the version-2 identity.
 *
 * <p>The {@code schemaVersion} is fixed at {@link #SCHEMA_VERSION 2}: this is the version-2 audit
 * event schema, which adds the schema version and the owner segment to the version-1 shape.
 *
 * <p>The record is immutable: once constructed its fields cannot change, so a published event is a
 * faithful, tamper-proof record of the create at the moment it happened.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the created owner's id
 * @param memberId        the owner's current primary identifier (the unified member id)
 * @param ownerSegment    the owner's marketing segment, recomputed from the version-2 identity
 * @param membershipLevel the owner's derived membership level
 * @param schemaVersion   the audit event schema version, always {@link #SCHEMA_VERSION}
 * @param event           the event type, always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, String ownerSegment,
                                Integer membershipLevel, int schemaVersion, String event) {

    /** The event type discriminator carried by every owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The audit event schema version carried by every owner-created event (version 2). */
    public static final int SCHEMA_VERSION = 2;

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    /**
     * Builds an owner-created event ({@code event} fixed to {@link #OWNER_CREATED},
     * {@code schemaVersion} fixed to {@link #SCHEMA_VERSION}).
     *
     * @param seq             the monotonically increasing sequence number for this create
     * @param ownerId         the created owner's id
     * @param memberId        the owner's current primary identifier (the unified member id)
     * @param ownerSegment    the owner's marketing segment, recomputed from the version-2 identity
     * @param membershipLevel the owner's derived membership level
     * @return the immutable event
     */
    public static OwnerCreatedEvent of(long seq, Integer ownerId, String memberId,
                                       String ownerSegment, Integer membershipLevel) {
        return new OwnerCreatedEvent(seq, ownerId, memberId, ownerSegment, membershipLevel,
            SCHEMA_VERSION, OWNER_CREATED);
    }

    /**
     * Renders this event as a compact JSON object suitable for the structured audit log line.
     *
     * @return the JSON representation of this event
     */
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
