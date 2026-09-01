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

package org.springframework.samples.petclinic.rest.advice;

import java.util.concurrent.atomic.AtomicLong;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Emits an immutable, structured {@code OWNER_CREATED} event on the {@code AUDIT} logger whenever a
 * new owner is successfully created, carrying a monotonically increasing {@code seq}, the owner id,
 * the owner's current primary identifier and the membershipLevel. The identifier is read straight
 * from the owner, so it tracks whatever the primary identifier is: the customerCode today, and the
 * memberId once the customerCode is unified into it. Kept as its own small aspect so this rule stays
 * a self-contained unit rather than growing the controller, service or the plain audit line.
 */
@Aspect
@Component
public class OwnerCreatedEventAdvice {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AtomicLong seq = new AtomicLong();

    @Around("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public Object emitCreatedEvent(ProceedingJoinPoint joinPoint, Owner owner) throws Throwable {
        boolean created = owner.isNew();
        Object result = joinPoint.proceed();
        if (created) {
            AUDIT.info(new OwnerCreatedEvent(seq.incrementAndGet(), owner.getId(),
                owner.getCustomerCode(), OwnerMembershipLevel.of(owner), OwnerSegment.of(owner)).toJson());
        }
        return result;
    }
}

/**
 * Immutable structured owner-creation event, schema version 2. {@code identifier} is the owner's
 * current primary identifier (the version-2 memberId); {@code segment} is the owner segment recomputed
 * from that version-2 identity. {@link #toJson()} renders the identifier under the {@code customerCode}
 * key the audit contract names and carries the {@code schemaVersion} and {@code ownerSegment} fields.
 */
record OwnerCreatedEvent(long seq, Integer ownerId, String identifier, int membershipLevel, String segment) {

    /** The audit event schema version; 2 since the version-2 owner identity release. */
    static final int SCHEMA_VERSION = 2;

    String toJson() {
        return "{\"schemaVersion\":" + SCHEMA_VERSION
            + ",\"seq\":" + seq
            + ",\"ownerId\":" + ownerId
            + ",\"customerCode\":" + quote(identifier)
            + ",\"membershipLevel\":" + membershipLevel
            + ",\"ownerSegment\":" + quote(segment)
            + ",\"event\":\"OWNER_CREATED\"}";
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
