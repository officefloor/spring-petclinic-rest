package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single duplicate-detection key for an owner:
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two owners
 * are duplicates only when their whole identity keys are equal.
 */
final class IdentityKey {

    private IdentityKey() {
    }

    static String of(Owner owner) {
        return digits(owner.getTelephone()) + '|' + orEmpty(owner.getEmail()) + '|' + orEmpty(owner.getHouseholdId());
    }

    private static String digits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
