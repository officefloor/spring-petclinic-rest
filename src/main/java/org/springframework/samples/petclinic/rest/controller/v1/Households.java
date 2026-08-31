package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's deterministic {@code householdId} and detects household membership.
 * Owners that carry the same normalized last name and postcode belong to the same
 * household, so they automatically share the same id.
 */
final class Households {

    private Households() {
    }

    /** Trim, collapse internal whitespace and lower-case a value for comparison. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * The stable household id for an owner: the first 12 hex characters of SHA-256 over
     * {@code (normalizedLastName + '|' + postcode)}. Owners with the same last name and
     * postcode share it automatically.
     */
    static String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        return sha256hex(normalize(owner.getLastName()) + "|" + postcode).substring(0, 12);
    }

    /** How many of the given owners belong to the candidate's household (share its household id). */
    static long memberCount(Collection<Owner> owners, Owner candidate) {
        String id = householdId(candidate);
        return owners.stream().filter(o -> id.equals(householdId(o))).count();
    }

    /** True when an existing owner already belongs to the candidate's household. */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String id = householdId(candidate);
        return existing.stream().anyMatch(o -> id.equals(householdId(o)));
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code s}. */
    private static String sha256hex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
