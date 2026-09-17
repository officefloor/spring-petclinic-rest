package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's current primary identifier — the single value that identifies an owner across the
 * system. Now that the former customerCode and membershipNumber are unified, that is the
 * {@link MemberId memberId}. Callers that record "the primary identifier" (such as the owner-created
 * audit event) go through here, so the identity is sourced in one place rather than spread across
 * every consumer.
 */
public final class PrimaryIdentifier {

    private PrimaryIdentifier() {
    }

    /** The owner's current primary identifier, or {@code null} when the owner has none. */
    public static String of(Owner owner) {
        return owner.getMemberId();
    }
}
