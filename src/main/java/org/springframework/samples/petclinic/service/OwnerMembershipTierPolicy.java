package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code membershipTier} is {@code SILVER} when it has no namesakes
 * (namesakeCount is 0) and an email is present, and {@code BRONZE} otherwise. Kept as a small,
 * self-contained unit so the rule can be applied from the read flow without adding complexity to
 * the mapper, controller, or service.
 */
public final class OwnerMembershipTierPolicy {

    private OwnerMembershipTierPolicy() {
    }

    /**
     * Derive the {@code membershipTier} for the given owner.
     *
     * @param owner the owner whose membership tier to derive
     * @return {@code "SILVER"} when the owner has no namesakes and an email, otherwise {@code "BRONZE"}
     */
    public static String membershipTier(Owner owner) {
        Integer namesakeCount = owner.getNamesakeCount();
        boolean silver = namesakeCount != null && namesakeCount == 0
            && owner.getEmail() != null && !owner.getEmail().isEmpty();
        return silver ? "SILVER" : "BRONZE";
    }
}
