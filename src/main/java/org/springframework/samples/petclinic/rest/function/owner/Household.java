package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Identity of an owner's household. Two owners live in the same household when they share
 * the same last name and address, compared case-insensitively with collapsed whitespace.
 *
 * <p>The {@link #id(String, String) household id} is derived deterministically from that
 * canonical key, so every member of a household computes the same stable identifier without
 * needing to coordinate.
 */
final class Household {

    private Household() {
    }

    /**
     * Canonical household key: last name and address, each trimmed, with internal whitespace
     * collapsed and lower-cased, joined so distinct pairs never collide.
     */
    static String key(String lastName, String address) {
        return canonical(lastName) + "\n" + canonical(address);
    }

    /** Whether the two (last name, address) pairs belong to the same household. */
    static boolean same(String lastNameA, String addressA, String lastNameB, String addressB) {
        return key(lastNameA, addressA).equals(key(lastNameB, addressB));
    }

    /**
     * Stable identifier shared by every owner in the household — the SHA-256 hex digest of the
     * canonical {@link #key(String, String) key}, so any member derives the same value.
     */
    static String id(String lastName, String address) {
        return sha256Hex(key(lastName, address));
    }

    /** Case-insensitive form with leading/trailing and repeated internal whitespace collapsed. */
    private static String canonical(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
