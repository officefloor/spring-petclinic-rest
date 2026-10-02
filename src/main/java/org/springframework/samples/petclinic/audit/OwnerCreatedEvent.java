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
 * <p>The {@code customerCode} component carries the owner's <em>current primary
 * identifier</em>: today that is the {@link Owner#getCustomerCode() customerCode},
 * but it is sourced through {@link #primaryIdentifier(Owner)} so that when the
 * customerCode is later unified into the memberId the event automatically carries
 * the memberId instead — the single place to change lives there, not at the call site.
 *
 * <p>Being a {@code record} the event is immutable once published.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel, String event) {

    /** The fixed {@code event} discriminator for an owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Build the event for a freshly created {@code owner}, stamping it with the given
     * monotonically increasing {@code seq} and {@code membershipLevel}.
     *
     * <p>{@code ownerId} and the primary identifier are read straight from the owner, but
     * {@code membershipLevel} is supplied pre-derived by the caller on purpose: deriving it
     * requires {@code OwnerMapper} (the {@code mapper} package), and this {@code audit}
     * package deliberately does not depend on it. Do not "simplify" this by pulling the
     * mapper in here.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, Integer membershipLevel) {
        return new OwnerCreatedEvent(seq, owner.getId(), primaryIdentifier(owner),
                membershipLevel, OWNER_CREATED);
    }

    /**
     * The owner's current primary identifier. Today this is the customerCode; when the
     * customerCode is unified into the memberId, change this one expression and every
     * emitted event carries the memberId instead.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
