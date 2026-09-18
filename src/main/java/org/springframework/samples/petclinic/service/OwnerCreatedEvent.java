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

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event recording that an {@link Owner} was created.
 *
 * <p>This is schema version {@value IdentityVersion#NUMBER} of the event: it stamps its own
 * {@code schemaVersion} and carries the owner's marketing {@code ownerSegment}, recomputed from the
 * owner's current (version-2) identity. The event also carries the owner's
 * {@linkplain Owner#getPrimaryIdentifier() current primary identifier} (the member id) so downstream
 * consumers always see the identifier the owner is currently keyed on. The {@code seq} orders creates
 * monotonically. Being a record, the event is immutable once emitted.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
                                Integer membershipLevel, String ownerSegment, String event) {

    /** The single event type emitted by this record. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Capture the {@code OWNER_CREATED} event for the given owner at the given sequence number.
     *
     * @param seq   the monotonic create sequence number
     * @param owner the newly persisted owner
     */
    public OwnerCreatedEvent(long seq, Owner owner) {
        this(IdentityVersion.NUMBER, seq, owner.getId(), owner.getPrimaryIdentifier(),
            owner.getMembershipLevel(), owner.getOwnerSegment(), OWNER_CREATED);
    }
}
