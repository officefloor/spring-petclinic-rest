package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Household-identity rule for owners: two owners share a household when they have
 * the same last name and the same address, compared case-insensitively with
 * collapsed whitespace.
 */
final class Households {

    private Households() {
    }

    /**
     * Whether {@code candidate} duplicates the household of an existing owner and so
     * should be rejected. A {@code sharesHousehold} flag of {@code true} opts out.
     */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return false;
        }
        return existing.stream().anyMatch(other -> sameHousehold(other, candidate));
    }

    private static boolean sameHousehold(Owner a, Owner b) {
        return normalize(a.getLastName()).equals(normalize(b.getLastName()))
            && normalize(a.getAddress()).equals(normalize(b.getAddress()));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
