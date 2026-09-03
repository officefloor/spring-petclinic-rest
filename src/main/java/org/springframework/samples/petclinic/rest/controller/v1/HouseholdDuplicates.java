package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    /**
     * When {@code candidate} opted in via {@code sharesHousehold} and joins an existing owner's
     * household (same last name and address), stamps it with the household's stable shared id.
     */
    static void assignHousehold(Owner candidate, Collection<Owner> existing, Boolean sharesHousehold) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String key = key(candidate);
        if (existing.stream().anyMatch(other -> key(other).equals(key))) {
            candidate.setHouseholdId(householdId(key));
        }
    }

    private static String householdId(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String key(Owner owner) {
        return norm(owner.getLastName()) + "\n" + norm(owner.getAddress());
    }

    private static String norm(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
