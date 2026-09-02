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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.stereotype.Component;

/**
 * Publishes an immutable structured {@code OWNER_CREATED} event to the AUDIT logger, alongside the
 * human-readable create audit line. Highest precedence so this after-returning advice runs once the
 * membershipLevel has been populated on the response. The event carries the owner's current primary
 * identifier: the customerCode today, and whatever replaces it later (e.g. the memberId).
 */
@Aspect
@Component
@Order(-1)
public class OwnerCreatedEventAdvice {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..))", returning = "response")
    public void publishCreated(ResponseEntity<?> response) {
        if (response != null && response.getBody() instanceof OwnerDto owner) {
            String memberId = owner.getIdentity() == null ? null : owner.getIdentity().getMemberId();
            AUDIT.info(new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                memberId, owner.getMembershipLevel(), OwnerSegment.of(owner)).toJson());
        }
    }

    /**
     * The immutable schema-version-2 audit event; {@code identifier} is the owner's current primary
     * identifier and {@code segment} the version-2 owner segment recomputed from the response.
     */
    private record OwnerCreatedEvent(long seq, Integer ownerId, String identifier, Integer membershipLevel,
            String segment) {

        String toJson() {
            return String.format("{\"schemaVersion\":2,\"seq\":%d,\"ownerId\":%s,\"memberId\":%s,"
                + "\"membershipLevel\":%s,\"ownerSegment\":%s,\"event\":\"OWNER_CREATED\"}",
                seq, ownerId, quote(identifier), membershipLevel, quote(segment));
        }

        private static String quote(String value) {
            return value == null ? "null" : "\"" + value + "\"";
        }
    }
}
