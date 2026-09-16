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

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Writes owner-lifecycle audit records to the dedicated {@code AUDIT} logger, keeping the
 * audit trail decoupled from the application's diagnostic logging. Each successful owner
 * creation produces a human-readable line carrying the identifying facts of the new record,
 * followed by an immutable structured {@link OwnerCreatedEvent} serialized as JSON.
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Monotonically increasing sequence number stamped on each owner-creation event. */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Emits the audit records for a newly created owner: a human-readable line recording its id,
     * member id, registration date and membership level, followed by an immutable structured
     * {@link OwnerCreatedEvent} (as JSON) carrying the owner's id and primary identifier.
     *
     * @param owner the persisted owner (must already have an assigned id)
     */
    public void logCreated(Owner owner) {
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), owner.getMembershipLevel());
        OwnerCreatedEvent event = new OwnerCreatedEvent(sequence.incrementAndGet(), owner.getId(),
            owner.getPrimaryIdentifier(), owner.getMembershipLevel(), OwnerCreatedEvent.TYPE);
        AUDIT.info("{}", MAPPER.writeValueAsString(event));
    }
}
