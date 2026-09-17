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
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Emits audit records for owner life-cycle events to the dedicated {@code AUDIT}
 * logger, keeping audit side-effects separate from the create business logic.
 */
@Component
public class OwnerAuditor {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final OwnerMapper ownerMapper;

    private final ObjectMapper jsonMapper = JsonMapper.builder().build();

    /** Sequence shared by every owner-created event, monotonically increasing across creates. */
    private final AtomicLong sequence = new AtomicLong();

    public OwnerAuditor(OwnerMapper ownerMapper) {
        this.ownerMapper = ownerMapper;
    }

    /**
     * Record the successful creation of an owner: a human-readable audit line plus an immutable
     * structured {@link OwnerCreatedEvent} serialized as JSON, both on the {@code AUDIT} logger.
     */
    public void auditCreated(Owner owner) {
        Integer membershipLevel = this.ownerMapper.resolveMembershipLevel(owner);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        OwnerCreatedEvent event = new OwnerCreatedEvent(this.sequence.incrementAndGet(),
            owner.getId(), owner.getMemberId(), membershipLevel);
        AUDIT.info(this.jsonMapper.writeValueAsString(event));
    }
}
