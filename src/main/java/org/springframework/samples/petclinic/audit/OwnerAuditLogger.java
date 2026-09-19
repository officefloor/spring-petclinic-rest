/*
 * Copyright 2002-2013 the original author or authors.
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
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipNumberFormatter;
import org.springframework.samples.petclinic.util.OwnerMembership;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Emits audit records for owner lifecycle events to the dedicated {@code AUDIT} logger,
 * keeping audit-trail concerns out of the business logic that triggers them.
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper EVENT_MAPPER = JsonMapper.builder().build();

    /** Source of the monotonically increasing sequence number carried by each event. */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Record that an owner was successfully created. Emits both the human-readable
     * audit line and an immutable structured {@link OwnerCreatedEvent} as JSON, so
     * downstream consumers can process the event without parsing the free-form line.
     *
     * @param owner the persisted owner (with its generated id) to audit
     */
    public void ownerCreated(Owner owner) {
        AUDIT.info(
            "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            membershipLevel(owner), membershipNumber(owner));
        OwnerCreatedEvent event = new OwnerCreatedEvent(
            sequence.incrementAndGet(), owner.getId(), primaryIdentifier(owner), membershipLevel(owner));
        AUDIT.info(EVENT_MAPPER.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier. This is the single place that changes
     * when the customer code is unified into the member id: return {@code owner}'s
     * member id instead and the emitted event carries it automatically.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    private static int membershipLevel(Owner owner) {
        return OwnerMembership.level(owner);
    }

    private static String membershipNumber(Owner owner) {
        return MembershipNumberFormatter.format(owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
