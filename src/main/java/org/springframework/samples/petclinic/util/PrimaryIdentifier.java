package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's current primary identifier — the single value that identifies an owner across the
 * system. Today that is the {@link CustomerCode customerCode}; when identities are unified this will
 * resolve to the memberId instead. Callers that record "the primary identifier" (such as the
 * owner-created audit event) go through here, so the switch is a one-line change confined to this
 * class rather than spread across every consumer.
 */
public final class PrimaryIdentifier {

    private PrimaryIdentifier() {
    }

    /** The owner's current primary identifier, or {@code null} when the owner has none. */
    public static String of(Owner owner) {
        return owner.getCustomerCode();
    }
}
