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
package org.springframework.samples.petclinic.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the audit records for a newly created owner to the {@code AUDIT} logger: a
 * human-readable line and an immutable, structured {@code OWNER_CREATED} event carrying a
 * monotonically increasing {@code seq} across all creates, the owner id, and the owner's
 * current primary identifier, the {@code memberId}.
 */
public final class OwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicInteger SEQ = new AtomicInteger();

    private OwnerCreatedEvent() {
    }

    public static void emit(Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), primaryId(owner), owner.getRegistrationDate(), MembershipLevel.of(owner));
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), primaryId(owner), owner.getMembershipLevel());
    }

    private static String primaryId(Owner owner) {
        return owner.getMemberId();
    }
}
