package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Shared household matching: canonicalizes the free-text fields that identify a household
 * (last name and address) so values that differ only in letter case or in the amount of
 * surrounding/internal whitespace compare equal. Used to detect when a new owner shares a
 * household with an existing one, and to derive the stable identifier that owners in the
 * same household share.
 */
final class Households {

    private Households() {
    }

    /**
     * Canonicalizes {@code value} for case-insensitive, whitespace-insensitive comparison:
     * trims the ends, collapses every run of whitespace to a single space and lower-cases
     * the result. Returns an empty string when {@code value} is {@code null}.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Whether {@code owner} belongs to the household identified by {@code lastName} and
     * {@code address}, comparing both fields canonically (see {@link #normalize(String)}).
     */
    static boolean matches(Owner owner, String lastName, String address) {
        return normalize(lastName).equals(normalize(owner.getLastName()))
                && normalize(address).equals(normalize(owner.getAddress()));
    }

    /**
     * The stable identifier shared by every owner in the household identified by
     * {@code lastName} and {@code address}. Derived purely from the canonical household key,
     * so the same household always yields the same id without any coordination.
     */
    static String id(String lastName, String address) {
        String key = normalize(lastName) + "\n" + normalize(address);
        return "H-" + sha256Hex(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
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
}
