package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Shared household identity for the create-owner pipeline: derives a canonical key from an
 * owner's last name and address so the duplicate-household check compares the same value.
 *
 * <p>Two owners share a household when their last names match case-insensitively (with
 * collapsed whitespace) and their addresses match once put through the shared
 * {@link OwnerAddresses#normalize(String) address normalization}.
 */
final class OwnerHouseholds {

    private OwnerHouseholds() {
    }

    /**
     * Canonical household key for {@code lastName} + {@code address}. Two requests/owners
     * belong to the same household exactly when their keys are equal.
     */
    static String key(String lastName, String address) {
        // '\n' separates the fields so "a b" + "c" cannot collide with "a" + "b c"
        // (a newline never survives whitespace collapsing).
        return normalizeLastName(lastName) + "\n" + OwnerAddresses.normalize(address);
    }

    /**
     * Stable, shared household identifier for {@code lastName} + {@code address}, formatted
     * {@code HH-<12 hex>}. Derived from the canonical {@link #key(String, String)}, so every
     * owner in the same household resolves to the same value regardless of when they are
     * created.
     */
    static String id(String lastName, String address) {
        return "HH-" + sha256Hex(key(lastName, address)).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
