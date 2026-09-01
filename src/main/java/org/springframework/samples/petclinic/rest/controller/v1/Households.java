package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    /**
     * When {@code candidate} opts to share a household, stamp it and every existing
     * housemate with the same stable {@code householdId} and return the housemates so
     * the caller can persist them. Returns an empty list when nothing is shared.
     */
    static List<Owner> joinHousehold(Collection<Owner> existing, Owner candidate, Boolean sharesHousehold) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return List.of();
        }
        List<Owner> housemates = existing.stream().filter(other -> sameHousehold(other, candidate)).toList();
        if (!housemates.isEmpty()) {
            String householdId = householdId(candidate);
            candidate.setHouseholdId(householdId);
            housemates.forEach(mate -> mate.setHouseholdId(householdId));
        }
        return housemates;
    }

    /** A stable identifier derived from the normalized household key, shared by all its members. */
    private static String householdId(Owner owner) {
        String key = normalize(owner.getLastName()) + "|" + normalize(owner.getAddress());
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static boolean sameHousehold(Owner a, Owner b) {
        return normalize(a.getLastName()).equals(normalize(b.getLastName()))
            && normalize(a.getAddress()).equals(normalize(b.getAddress()));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
