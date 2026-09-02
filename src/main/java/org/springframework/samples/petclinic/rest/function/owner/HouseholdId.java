package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Deterministic household identifier: the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode}. Owners with the same last name and postcode share it,
 * so it keys household duplicate detection and the household-size count.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(String lastName, String postcode) {
        String key = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
