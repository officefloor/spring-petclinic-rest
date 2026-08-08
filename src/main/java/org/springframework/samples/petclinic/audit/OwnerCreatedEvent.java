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
package org.springframework.samples.petclinic.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;

/**
 * Immutable structured audit event emitted when an owner is created.
 *
 * <p>It carries the monotonically increasing create sequence number, the owner id, the membership
 * level, and — crucially — the owner's <em>current primary identifier</em>. Today that identifier is
 * the {@code customerCode}, and that is the JSON key it is published under; when the customerCode is
 * later unified into the memberId the event will simply carry the memberId (callers always pass
 * whatever the current primary identifier is, so only the key name changes here).
 */
public final class OwnerCreatedEvent {

    /** Fixed discriminator identifying this event type in the audit stream. */
    public static final String EVENT = "OWNER_CREATED";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final long seq;

    private final Integer ownerId;

    private final String primaryIdentifier;

    private final Integer membershipLevel;

    public OwnerCreatedEvent(long seq, Integer ownerId, String primaryIdentifier, Integer membershipLevel) {
        this.seq = seq;
        this.ownerId = ownerId;
        this.primaryIdentifier = primaryIdentifier;
        this.membershipLevel = membershipLevel;
    }

    public long getSeq() {
        return this.seq;
    }

    public Integer getOwnerId() {
        return this.ownerId;
    }

    /** The owner's current primary identifier at the time of the event (currently the customerCode). */
    public String getPrimaryIdentifier() {
        return this.primaryIdentifier;
    }

    public Integer getMembershipLevel() {
        return this.membershipLevel;
    }

    /**
     * Render this event as a compact JSON object with a stable field order:
     * {@code {seq, ownerId, customerCode, membershipLevel, event}}. The primary identifier is
     * currently published under the {@code customerCode} key.
     */
    public String toJson() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("seq", this.seq);
        fields.put("ownerId", this.ownerId);
        fields.put("customerCode", this.primaryIdentifier);
        fields.put("membershipLevel", this.membershipLevel);
        fields.put("event", EVENT);
        return MAPPER.writeValueAsString(fields);
    }
}
