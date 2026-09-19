/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Emits the audit trail for owner creation.
 *
 * <p>Successful creations are written to the dedicated {@code AUDIT} logger so the
 * side-effect can be routed and asserted independently of application logging. Each
 * creation emits both a human-readable audit line and an immutable structured
 * {@link OwnerCreatedEvent} serialized as JSON.
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /** Source of the monotonically increasing sequence number carried by each structured event. */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Record that an owner was successfully created.
     *
     * @param owner the persisted owner, with its generated id, member id, registration date
     *              and membership level
     */
    public void logCreated(Owner owner) {
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
            owner.getMembershipLevel());
        OwnerCreatedEvent event = OwnerCreatedEvent.of(this.sequence.incrementAndGet(), owner);
        AUDIT.info(MAPPER.writeValueAsString(event));
    }
}
