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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted once per owner create, alongside the
 * human-readable audit line.
 *
 * <p>The {@code memberId} component carries the owner's <em>current primary
 * identifier</em>, the unified {@link Owner#getMemberId() memberId}, sourced through
 * {@link #primaryIdentifier(Owner)} so the single place to change it lives there, not
 * at the call site.
 *
 * <p>This is the schema-version-2 event: it carries an explicit {@code schemaVersion} of 2
 * and the owner's {@code ownerSegment}, recomputed from the owner's version-2 identity by the
 * caller and supplied pre-derived for the same reason {@code membershipLevel} is.
 *
 * <p>Being a {@code record} the event is immutable once published.
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    /** The fixed {@code event} discriminator for an owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The schema version of the structured owner-created event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Build the event for a freshly created {@code owner}, stamping it with the given
     * monotonically increasing {@code seq}, {@code membershipLevel} and {@code ownerSegment},
     * at the current {@link #SCHEMA_VERSION}.
     *
     * <p>{@code ownerId} and the primary identifier are read straight from the owner, but
     * {@code membershipLevel} and {@code ownerSegment} are supplied pre-derived by the caller
     * on purpose: deriving them requires {@code OwnerMapper} (the {@code mapper} package), and
     * this {@code audit} package deliberately does not depend on it. Do not "simplify" this by
     * pulling the mapper in here.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, Integer membershipLevel, String ownerSegment) {
        return new OwnerCreatedEvent(seq, SCHEMA_VERSION, owner.getId(), primaryIdentifier(owner),
                membershipLevel, ownerSegment, OWNER_CREATED);
    }

    /**
     * The owner's current primary identifier, the unified memberId. Change this one expression
     * to re-source every emitted event's primary identifier.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
