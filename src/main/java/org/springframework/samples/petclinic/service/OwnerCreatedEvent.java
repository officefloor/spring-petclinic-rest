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

/**
 * Immutable structured record of an owner's creation, emitted to the {@code AUDIT} logger as JSON.
 * The {@code memberId} field carries the owner's {@linkplain
 * org.springframework.samples.petclinic.model.Owner#getMemberId() member id}, its stable primary
 * identifier.
 *
 * @param seq            monotonically increasing sequence number across all creates
 * @param ownerId        the created owner's id
 * @param memberId       the owner's member id at creation time
 * @param membershipLevel the owner's resolved membership level
 * @param event          the event type discriminator, always {@code "OWNER_CREATED"}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String event) {

    /** The event-type discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, EVENT_TYPE);
    }
}
