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
package org.springframework.samples.petclinic.model;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry to the dedicated {@code AUDIT} logger whenever an
 * owner is first persisted, recording the assigned id, member id and
 * registration date. It also emits an immutable structured {@code OWNER_CREATED}
 * JSON event carrying the owner's current primary identifier (the member id).
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence stamped on each structured create event. */
    private static final AtomicLong SEQ = new AtomicLong();

    @PostPersist
    void onCreate(Owner owner) {
        int membershipLevel = MembershipLevel.of(MembershipLevel.points(owner.getNamesakeCount(),
            owner.getEmail(), owner.getHouseholdMemberCount(), owner.getRegistrationDate()));
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        AUDIT.info("{}", String.format(
            "{\"seq\":%d,\"ownerId\":%d,\"memberId\":\"%s\",\"membershipLevel\":%d,\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), owner.getMemberId(), membershipLevel));
    }
}
