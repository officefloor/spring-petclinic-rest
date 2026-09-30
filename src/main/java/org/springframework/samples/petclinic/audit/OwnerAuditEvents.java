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

package org.springframework.samples.petclinic.audit;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits immutable structured audit events to the dedicated {@code AUDIT} logger. The sequence
 * counter, the event shape and its JSON serialization are a single cohesive concern kept here,
 * off the REST controller and on the standard Jackson path this codebase already uses.
 */
@Component
public class OwnerAuditEvents {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence stamped onto every OWNER_CREATED event, across all creates. */
    private final AtomicLong seq = new AtomicLong();

    private final ObjectMapper objectMapper;

    public OwnerAuditEvents(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** The schema version stamped onto every structured audit event. Version 2 accompanies the
     *  version-2 owner identity: the {@code memberId} is the version-2 member id and the event now
     *  carries the {@code ownerSegment} recomputed from that version-2 identity. */
    private static final int SCHEMA_VERSION = 2;

    /**
     * Emit the immutable {@code OWNER_CREATED} event, stamping a fresh monotonically increasing
     * {@code seq} and the current {@link #SCHEMA_VERSION}. The {@code memberId} argument is the
     * owner's primary identifier (see {@code Owner#primaryIdentifier}), i.e. its version-2 member id,
     * and {@code ownerSegment} is the segment recomputed from that version-2 identity.
     */
    public void ownerCreated(Integer ownerId, String memberId, Integer membershipLevel, String ownerSegment) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            seq.incrementAndGet(), SCHEMA_VERSION, ownerId, memberId, membershipLevel,
            ownerSegment, "OWNER_CREATED");
        auditLogger.info(objectMapper.writeValueAsString(event));
    }

    /** Immutable structured OWNER_CREATED audit event (schema version 2). */
    record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
                             Integer membershipLevel, String ownerSegment, String event) {
    }
}
