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
import org.springframework.samples.petclinic.service.MembershipLevelEvaluator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Emits audit output to the dedicated {@code AUDIT} logger whenever an owner is successfully
 * created. Two entries are produced per create:
 * <ul>
 *   <li>a human-readable line carrying the persisted owner's id, its generated
 *       {@code memberId}, its {@code registrationDate} and its derived
 *       {@code membershipLevel}; and</li>
 *   <li>an immutable structured {@link OwnerCreatedEvent} serialized as JSON, so the create
 *       can be reconciled against the audit log without depending on any
 *       implementation-specific hook.</li>
 * </ul>
 */
@Component
public class OwnerAuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final MembershipLevelEvaluator membershipLevelEvaluator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Assigns each created owner a monotonically increasing sequence number. */
    private final AtomicLong sequence = new AtomicLong();

    public OwnerAuditLogger(MembershipLevelEvaluator membershipLevelEvaluator) {
        this.membershipLevelEvaluator = membershipLevelEvaluator;
    }

    /**
     * Record the successful creation of an owner.
     *
     * @param owner the persisted owner (id, memberId and registrationDate populated)
     */
    public void logCreated(Owner owner) {
        Integer membershipLevel = membershipLevelEvaluator.levelFor(owner);
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        OwnerCreatedEvent event = OwnerCreatedEvent.of(sequence.incrementAndGet(), owner, membershipLevel);
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
