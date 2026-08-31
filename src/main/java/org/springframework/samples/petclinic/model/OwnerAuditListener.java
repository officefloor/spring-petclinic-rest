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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.samples.petclinic.util.Membership;

import jakarta.persistence.PostPersist;

/**
 * Emits an audit trail entry when an {@link Owner} is created. On a successful
 * insert the {@code AUDIT} logger records the new owner's id, customer code and
 * registration date.
 */
public class OwnerAuditListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    private static final java.util.concurrent.atomic.AtomicLong OWNER_SEQ = new java.util.concurrent.atomic.AtomicLong();

    @PostPersist
    void onCreate(Owner owner) {
        String memberId = org.springframework.samples.petclinic.util.MemberIds.of(owner);
        int membershipLevel = Membership.level(Membership.points(owner, false, false));
        String ownerSegment = org.springframework.samples.petclinic.util.Segments.of(membershipLevel,
            org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode()));
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), memberId);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), memberId, owner.getRegistrationDate(), membershipLevel);
        AUDIT.info("{\"schemaVersion\":2,\"seq\":{},\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},\"ownerSegment\":\"{}\",\"event\":\"OWNER_CREATED\"}",
            OWNER_SEQ.incrementAndGet(), owner.getId(), memberId, membershipLevel, ownerSegment);
    }

}
