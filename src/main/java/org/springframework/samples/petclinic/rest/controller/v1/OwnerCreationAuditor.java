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

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Records the audit trail for a freshly-created owner. The create-audit concern is
 * gathered here, off the {@link OwnerRestControllerV1 controller}, so the controller
 * stays a thin entry point and everything written to the dedicated {@code AUDIT}
 * logger for an owner create — its format and the owner attributes it reports — lives
 * together in one place, free to grow without crowding the request handling.
 */
@Component
public class OwnerCreationAuditor {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Monotonically increasing sequence number stamped on every OWNER_CREATED event. */
    private static final AtomicLong SEQ = new AtomicLong();

    /**
     * Write the create audit record for a freshly-saved owner. Keeps the audit format in
     * one place so it stays in step with the owner's identity: it reports the assigned
     * member id alongside the owner's id, registration date and membership level.
     *
     * <p>Alongside the human-readable line, the immutable structured
     * {@link #ownerCreatedEvent(Owner) OWNER_CREATED event} is emitted as a single-line JSON
     * object so downstream consumers have a stable, machine-readable record.
     *
     * @param owner the owner that has just been created and saved
     */
    public void auditOwnerCreated(Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), owner.getMembershipLevel());

        AUDIT.info(ownerCreatedEvent(owner).toString());
    }

    /**
     * Build the immutable structured {@code OWNER_CREATED} event for a freshly-saved owner as a
     * single JSON object, so downstream consumers have a stable, machine-readable record.
     * Gathered here, off {@link #auditOwnerCreated}, so the event's schema — the fields it
     * carries and their order — has a single home, free to grow without crowding the emission
     * itself. This is schema version 2: it stamps a {@code schemaVersion} of 2 and carries the
     * owner's primary identifier, the version-2 unified {@link #primaryIdentifier(Owner) member
     * id}, alongside the owner segment recomputed from that version-2 identity.
     *
     * @param owner the owner that has just been created and saved
     * @return the structured {@code OWNER_CREATED} event
     */
    private ObjectNode ownerCreatedEvent(Owner owner) {
        ObjectNode event = MAPPER.createObjectNode();
        event.put("schemaVersion", 2);
        event.put("seq", SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", primaryIdentifier(owner));
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("ownerSegment", owner.getOwnerSegment());
        event.put("event", "OWNER_CREATED");
        return event;
    }

    /**
     * The owner's primary identifier: the unified {@link Owner#getMemberId() member id}.
     * Isolated here so the {@code OWNER_CREATED} event has a single place that decides which
     * field is the owner's primary identifier.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
