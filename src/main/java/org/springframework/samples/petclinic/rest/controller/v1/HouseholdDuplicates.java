package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects owners that share a household, i.e. have the same last name and address
 * (compared case-insensitively with collapsed whitespace).
 */
final class HouseholdDuplicates {

    private HouseholdDuplicates() {
    }

    /**
     * Returns {@code true} when an existing owner shares {@code candidate}'s household and the
     * request did not opt in via {@code sharesHousehold}.
     */
    static boolean isRejectedDuplicate(Owner candidate, Collection<Owner> existing, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return false;
        }
        String key = key(candidate);
        return existing.stream().anyMatch(other -> key(other).equals(key));
    }

    private static String key(Owner owner) {
        return norm(owner.getLastName()) + "\n" + norm(owner.getAddress());
    }

    private static String norm(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
