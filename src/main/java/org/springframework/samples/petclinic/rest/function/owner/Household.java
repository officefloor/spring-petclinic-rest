package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Single source of truth for household identity. A household groups owners that share the same last
 * name and the same postcode, with the last name compared case-insensitively and with surrounding and
 * internal whitespace collapsed (so "Smith" / " smith " belong together).
 *
 * <p>The {@link #id(String, String)} derived from that normalized (lastName, postcode) pair is
 * deterministic: the same last name and postcode always map to the same value, so every owner at the
 * same household receives an identical {@code householdId} automatically — it is no longer something an
 * owner "joins" via {@code sharesHousehold}. The household is therefore the basis for duplicate
 * detection ({@link CheckHouseholdUnique}): two owners belong to the same household exactly when their
 * ids are equal.
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
     * The canonical household key for a (lastName, postcode) pair — the single definition of household
     * membership: {@code normalizedLastName + "|" + postcode}. Two owners belong to the same household
     * exactly when their keys are equal; {@link #id} hashes this same key, so the duplicate check and the
     * assigned id can never diverge. The last name is normalized (whitespace collapsed, lower-cased); the
     * postcode is trimmed and used as given (a {@code null} or blank postcode contributes an empty
     * component).
     */
    static String key(String lastName, String postcode) {
        String pc = (postcode == null) ? "" : postcode.trim();
        return normalize(lastName) + "|" + pc;
    }

    /**
     * Deterministic household identifier for the given last name and postcode: {@code "H-"} followed by
     * the first 12 hex characters of the SHA-256 of the {@link #key household key}.
     */
    static String id(String lastName, String postcode) {
        String key = key(lastName, postcode);
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
