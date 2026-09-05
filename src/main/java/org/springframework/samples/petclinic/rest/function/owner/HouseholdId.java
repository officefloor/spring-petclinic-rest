package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives the stable {@code householdId} shared by owners with the same lastName and
 * postcode. It is the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode}, where the lastName is compared
 * case-insensitively with collapsed whitespace, so co-resident same-lastName owners always
 * resolve to one identifier.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(String lastName, String postcode) {
        // 'V2' version tag mixed in so every version-2 householdId differs from version 1.
        String key = "V2|" + normalise(lastName) + '|' + (postcode == null ? "" : postcode);
        return sha256Hex(key).substring(0, 12);
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
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
