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

package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.samples.petclinic.util.MemberIds;
import org.springframework.samples.petclinic.util.OwnerSegments;

/**
 * Immutable structured audit event emitted when an owner is created. It carries
 * the owner's current primary identifier: the {@code customerCode} today, and
 * whatever replaces it later (e.g. a unified {@code memberId}), passed in by the
 * caller so this event stays agnostic to which identifier is primary. Emitted under
 * schema version 2, which adds the derived {@code ownerSegment} recomputed from the
 * version-2 identity's region.
 */
public record OwnerCreatedEvent(int seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String ownerSegment) {

    /** Audit schema version: 2 stamps every event with {@code schemaVersion} and the owner segment. */
    private static final int SCHEMA_VERSION = 2;

    private static final AtomicInteger SEQ = new AtomicInteger();

    /** Next event for an owner, stamped with a monotonically increasing {@code seq} across creates and
     *  the owner segment recomputed from the version-2 identity's region and the membership level. */
    public static OwnerCreatedEvent next(Integer ownerId, String primaryIdentifier, Integer membershipLevel) {
        String segment = OwnerSegments.of(membershipLevel == null ? 0 : membershipLevel,
            MemberIds.regionOf(primaryIdentifier));
        return new OwnerCreatedEvent(SEQ.incrementAndGet(), ownerId, primaryIdentifier, membershipLevel, segment);
    }

    /** Compact JSON rendering with a stable field order, led by the audit {@code schemaVersion}. */
    public String toJson() {
        return String.format(
            "{\"schemaVersion\":%d,\"seq\":%d,\"ownerId\":%s,\"customerCode\":%s,\"membershipLevel\":%s,\"ownerSegment\":%s,\"event\":\"OWNER_CREATED\"}",
            SCHEMA_VERSION, seq, ownerId, quote(customerCode), membershipLevel, quote(ownerSegment));
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
