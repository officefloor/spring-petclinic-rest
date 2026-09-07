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
     * customer code alongside the owner's id, registration date, membership level and
     * membership number.
     *
     * <p>Alongside the human-readable line, an immutable structured {@code OWNER_CREATED}
     * event is emitted as a single-line JSON object so downstream consumers have a stable,
     * machine-readable record. The event carries the owner's current primary identifier
     * (the customer code today); when that identifier is later unified into the member id
     * the event simply carries the member id instead — see {@link #primaryIdentifier(Owner)}.
     *
     * @param owner the owner that has just been created and saved
     */
    public void auditOwnerCreated(Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());

        ObjectNode event = MAPPER.createObjectNode();
        event.put("seq", SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("customerCode", primaryIdentifier(owner));
        event.put("membershipLevel", owner.getMembershipLevel());
        event.put("event", "OWNER_CREATED");
        AUDIT.info(event.toString());
    }

    /**
     * The owner's current primary identifier. Today that is the {@link Owner#getCustomerCode()
     * customer code}; when the customer code is later unified into the member id this is the one
     * place that changes so the {@code OWNER_CREATED} event follows the identity automatically.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
