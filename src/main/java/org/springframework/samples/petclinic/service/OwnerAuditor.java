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
import tools.jackson.databind.json.JsonMapper;

/**
 * Emits audit trail entries for {@link Owner} lifecycle events to the dedicated
 * {@code AUDIT} logger, keeping audit side-effects separate from request
 * handling and persistence logic.
 */
@Component
public class OwnerAuditor {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /** Monotonically increasing sequence number assigned to each create event. */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Record that an owner was successfully created, capturing its id, customer
     * code, (resolved) registration date, membership level and membership
     * number. Alongside the human-readable audit line, an immutable structured
     * {@link OwnerCreatedEvent} is emitted as JSON.
     *
     * @param owner the newly persisted owner
     */
    public void ownerCreated(Owner owner) {
        AUDIT.info(
            "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), owner.getMembershipLevel(),
            owner.getMembershipNumber());
        OwnerCreatedEvent event = new OwnerCreatedEvent(this.sequence.incrementAndGet(), owner);
        AUDIT.info(MAPPER.writeValueAsString(event));
    }
}
