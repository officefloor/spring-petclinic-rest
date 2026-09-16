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

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits audit-trail entries for owner lifecycle events to the dedicated {@code AUDIT}
 * logger, keeping audit side-effects out of the request-handling and persistence code.
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Monotonically increasing sequence stamped on each create event. */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Record the successful registration of a new owner. Besides the human-readable audit
     * line (capturing its id, member id, registration date and membership level), an
     * immutable schema-version-2 {@link OwnerCreatedEvent} carrying the owner's current
     * primary identifier and its recomputed owner segment is emitted as JSON on the same
     * trail.
     */
    public void logCreated(Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            owner.getMembershipLevel());
        OwnerCreatedEvent event = new OwnerCreatedEvent(sequence.incrementAndGet(),
            owner.getId(), owner.getPrimaryIdentifier(), owner.getMembershipLevel(),
            owner.getOwnerSegment());
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
