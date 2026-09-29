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

import org.springframework.samples.petclinic.rest.validation.IdentityVersion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Immutable structured audit event emitted via the {@code AUDIT} logger whenever an owner is
 * created. Serialized with the application's Jackson {@code ObjectMapper} it yields the JSON object
 * {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}; the key
 * order is pinned by {@link JsonPropertyOrder}.
 *
 * <p>This is schema version 2 of the event: {@code schemaVersion} is a fixed integer 2, matching the
 * version-2 owner identity, and the {@code memberId} it carries is the version-2 identifier.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates, so the ordering of events
 * is recoverable from the log alone. The event carries the owner's <em>current primary
 * identifier</em>, the unified {@code memberId}, so downstream consumers always see the identifier
 * that currently identifies the owner.
 *
 * <p>The record is immutable; once emitted its fields never change.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the persisted owner's id
 * @param memberId        the owner's current primary (version-2) identifier
 * @param membershipLevel the owner's capped numeric membership level (may be {@code null})
 */
@JsonPropertyOrder({"schemaVersion", "seq", "ownerId", "memberId", "membershipLevel", "event"})
record OwnerCreatedAuditEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {

    /** Discriminator value carried in the {@code event} field. */
    static final String EVENT = "OWNER_CREATED";

    /** The audit event schema version, aligned with the version-2 owner identity. */
    static final int SCHEMA_VERSION = IdentityVersion.API_VERSION;

    /** Constant schema version serialized as the {@code schemaVersion} property. */
    @JsonProperty("schemaVersion")
    int schemaVersion() {
        return SCHEMA_VERSION;
    }

    /** Constant discriminator serialized as the {@code event} property. */
    @JsonProperty("event")
    String event() {
        return EVENT;
    }
}
