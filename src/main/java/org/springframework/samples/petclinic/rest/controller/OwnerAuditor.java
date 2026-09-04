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

package org.springframework.samples.petclinic.rest.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Owns the owner-create audit concern: the single place that records the audit trail for a
 * successfully created {@link Owner}.
 *
 * <p>Keeping the audit trail here means every create side-effect that must be observable to an
 * auditor is emitted from one place, in one form, on the dedicated {@code AUDIT} logger — so the
 * registrar is left holding only the create pipeline, and what is audited on create can evolve
 * without threading logging detail back through it.
 */
@Component
public class OwnerAuditor {

    /** Dedicated audit logger; emits the audit trail on successful owner create. */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Serializes the structured owner-created event to a compact JSON object. */
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * Monotonically increasing sequence stamped onto each structured owner-created event. The
     * auditor is a singleton, so this counter runs across every create for the life of the
     * application, giving the emitted events a strict, gap-free order.
     */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Record the audit trail for an owner that has just been created: emit the audit line naming
     * the owner's id, member id, registration date and membership level, followed by the immutable
     * structured event carrying the same create. Called once, after the owner is persisted, with
     * the owner in its final stored form.
     *
     * @param owner           the freshly persisted owner
     * @param membershipLevel the owner's derived membership level, as computed for this create
     */
    public void ownerCreated(Owner owner, int membershipLevel) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        AUDIT.info(structuredEvent(owner, membershipLevel));
    }

    /**
     * Build the immutable structured owner-created event as a JSON object
     * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}. {@code seq} is
     * the next value of the monotonic {@link #sequence}, so every event carries a distinct, ordered
     * sequence number across creates.
     *
     * <p>{@code memberId} carries the owner's primary identifier, read from
     * {@link #primaryIdentifier} rather than being spelled out here.
     */
    private String structuredEvent(Owner owner, int membershipLevel) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("seq", sequence.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("memberId", primaryIdentifier(owner));
        event.put("membershipLevel", membershipLevel);
        event.put("event", "OWNER_CREATED");
        return MAPPER.writeValueAsString(event);
    }

    /**
     * The owner's primary identifier as it stands at this create: the unified member id. This is
     * the single point that decides which field is primary, so every emitted event carries the
     * member id.
     */
    private String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
