package org.springframework.samples.petclinic.model;

import java.util.Collection;

/**
 * Soft-duplicate detection for new owners. A new owner that is not a hard (identity-key)
 * duplicate is nevertheless a <em>possible</em> duplicate when it shares an existing owner's
 * last name and postcode but has a different telephone.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /** Id of the first existing owner the given owner possibly duplicates, or {@code null} if none. */
    public static Integer of(Owner owner, Collection<Owner> existing) {
        return existing.stream()
            .filter(other -> matches(owner, other))
            .map(Owner::getId)
            .findFirst()
            .orElse(null);
    }

    private static boolean matches(Owner owner, Owner other) {
        return owner.getPostcode() != null
            && owner.getPostcode().equalsIgnoreCase(other.getPostcode())
            && owner.getLastName().equalsIgnoreCase(other.getLastName())
            && !owner.getTelephone().equals(other.getTelephone());
    }
}
