package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership tier: {@code SILVER} for an owner with an email and no namesakes
 * ({@code namesakeCount} of 0), otherwise {@code BRONZE}.
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    public static String of(Owner owner, int namesakeCount) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return namesakeCount == 0 && hasEmail ? "SILVER" : "BRONZE";
    }
}
