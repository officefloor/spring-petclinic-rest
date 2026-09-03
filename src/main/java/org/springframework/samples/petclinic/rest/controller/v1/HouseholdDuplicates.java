package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Households are keyed deterministically on (normalized last name, postcode): every owner carries
 * the household id derived from those two fields, so owners that share them belong to the same
 * household automatically. {@code sharesHousehold} no longer creates the link (the id is always
 * computed); it only lets a further member of an existing household bypass the duplicate block.
 */
final class HouseholdDuplicates {

    private HouseholdDuplicates() {
    }

    /**
     * Stamps {@code candidate} with its deterministic household id (first 12 hex characters of
     * SHA-256 over {@code normalizedLastName + '|' + postcode}).
     */
    static void assignHousehold(Owner candidate, Collection<Owner> existing, Boolean sharesHousehold) {
        candidate.setHouseholdId(householdId(candidate));
    }

    /**
     * Returns {@code true} when an existing owner already belongs to {@code candidate}'s household
     * and the request did not opt in via {@code sharesHousehold}.
     */
    static boolean isRejectedDuplicate(Owner candidate, Collection<Owner> existing, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return false;
        }
        String id = householdId(candidate);
        return existing.stream().anyMatch(other -> id.equals(householdId(other)));
    }

    /** First 12 hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. */
    static String householdId(Owner owner) {
        String key = norm(owner.getLastName()) + "|" + blank(owner.getPostcode());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }

    private static String norm(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
