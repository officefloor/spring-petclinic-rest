package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Household identity for pet owners. Two owners belong to the same household when they share a last
 * name and address, compared case-insensitively with collapsed whitespace. The
 * {@link #id(String, String)} value is a stable identifier derived purely from those two fields, so
 * every owner in a household returns the same {@code householdId} without any stored state. Used by
 * {@link RequireUniqueHousehold} to detect duplicates and by the owner mapper to expose the
 * identifier on responses.
 */
public final class Household {

    private Household() {
    }

    /** Trim, collapse internal whitespace and lower-case for case-insensitive comparison. */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * A stable shared identifier for the household of an owner with the given last name and address.
     * Derived from the normalized fields, so owners in the same household get the same value.
     */
    public static String id(String lastName, String address) {
        String key = normalize(lastName) + "|" + normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
