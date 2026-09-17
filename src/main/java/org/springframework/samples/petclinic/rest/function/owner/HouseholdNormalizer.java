package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Canonical household comparison shared by the create pipeline: {@link EnsureUniqueHousehold} uses it
 * to decide whether two owners share a household and {@link AssignHousehold} uses it to derive the
 * shared household id. A household is identified by last name plus address, each compared
 * case-insensitively with runs of whitespace collapsed to a single space and outer whitespace
 * trimmed. Not a pipeline step, so it is free to expose plain helpers.
 */
public final class HouseholdNormalizer {

    private HouseholdNormalizer() {
    }

    /**
     * The stable household identifier derived from an owner's last name and address. Owners with
     * equal {@link #key(String, String) keys} share the same id, and the id never changes for a given
     * household, so it can be recomputed for late joiners rather than stored and looked up.
     */
    public static String id(String lastName, String address) {
        return "H-" + sha256hex(key(lastName, address)).substring(0, 12).toUpperCase();
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Builds the canonical household key for an owner from its last name and address. Two owners
     * belong to the same household when their keys are equal. Either field may be {@code null}, which
     * normalizes to an empty token.
     */
    public static String key(String lastName, String address) {
        // '\n' separates the fields so that a boundary shift (e.g. "ab"+"c" vs "a"+"bc") cannot forge a
        // collision; it never appears in a normalized value.
        return normalize(lastName) + "\n" + normalize(address);
    }

    /** Lower-cases and collapses whitespace to single spaces, trimming the ends; {@code null} → "". */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
