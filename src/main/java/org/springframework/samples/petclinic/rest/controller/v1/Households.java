package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Locale;
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Household-identity rule for owners: two owners share a household when they have the
 * same last name and postcode. The householdId is derived deterministically from that
 * pair, so housemates share it automatically without any explicit link step.
 */
final class Households {

    private Households() {
    }

    /**
     * The deterministic household identifier: the first 12 hex characters of
     * SHA-256 over {@code normalizedLastName + '|' + postcode}.
     */
    static String idFor(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        return sha256Hex12("V2|" + normalize(owner.getLastName()) + "|" + postcode);
    }

    /**
     * Whether {@code candidate} lands in an existing owner's household and so should be
     * rejected as a duplicate. A {@code sharesHousehold} flag of {@code true} opts out,
     * declaring the candidate a member of that household instead.
     */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return false;
        }
        return hasHousemate(existing, candidate);
    }

    /** Household size for {@code candidate}: its existing housemates plus itself. */
    static int size(Collection<Owner> existing, Owner candidate) {
        long housemates = existing.stream().filter(other -> sameHousehold(other, candidate)).count();
        return (int) housemates + 1;
    }

    /**
     * The membershipLevel ceiling for {@code candidate}: one above the highest membershipLevel
     * among its existing household members, or {@code null} when it has no household member yet
     * (in which case no cap applies).
     */
    static Integer ceilingLevel(Collection<Owner> existing, Owner candidate) {
        OptionalInt max = existing.stream()
            .filter(other -> sameHousehold(other, candidate))
            .mapToInt(Households::effectiveLevel)
            .max();
        return max.isPresent() ? max.getAsInt() + 1 : null;
    }

    /** An owner's membershipLevel after applying its own ceiling (see {@link #ceilingLevel}). */
    private static int effectiveLevel(Owner owner) {
        int level = MembershipLevels.levelOf(owner);
        Integer cap = owner.getMembershipLevelCap();
        return cap == null ? level : Math.min(level, cap);
    }

    /** Whether an existing owner already belongs to {@code candidate}'s household. */
    static boolean hasHousemate(Collection<Owner> existing, Owner candidate) {
        return existing.stream().anyMatch(other -> sameHousehold(other, candidate));
    }

    private static boolean sameHousehold(Owner a, Owner b) {
        return a.getHouseholdId() != null && a.getHouseholdId().equals(b.getHouseholdId());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex12(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(24);
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
