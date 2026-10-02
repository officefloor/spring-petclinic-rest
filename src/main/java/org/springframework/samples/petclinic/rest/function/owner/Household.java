package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Single source of truth for household identity. A household groups owners that share the same last
 * name and the same address, compared case-insensitively with surrounding and internal whitespace
 * collapsed (so "Smith" / " smith " and "10  Main St" / "10 Main St" belong together).
 *
 * <p>The {@link #id(String, String)} derived from that normalized pair is stable: the same last name
 * and address always map to the same value, so every member of a household — including one that joins
 * via {@code sharesHousehold} — receives an identical {@code householdId}.
 */
final class Household {

    private Household() {
    }

    /** Lower-case (locale-independent) with leading/trailing and repeated internal whitespace collapsed. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * The canonical household key for a (lastName, address) pair — the single definition of household
     * membership. Two owners belong to the same household exactly when their keys are equal; {@link #id}
     * hashes this same key, so the duplicate check and the assigned id can never diverge.
     *
     * <p>Addresses are compared in their {@link OwnerAddress normalized} form (whitespace collapsed,
     * upper-cased, abbreviations expanded) so that equivalent spellings — "12 main st" and
     * "12 MAIN STREET" — belong to the same household even when stored data was not normalized.
     */
    static String key(String lastName, String address) {
        return normalize(lastName) + "\n" + OwnerAddress.normalize(address);
    }

    /**
     * Stable household identifier for the given last name and address: {@code "H-"} followed by the
     * first 12 hex characters of the SHA-256 of the {@link #key household key}.
     */
    static String id(String lastName, String address) {
        String key = key(lastName, address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("H-");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
