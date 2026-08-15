package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Canonical household logic shared by the create-owner pipeline. A household is keyed on
 * {@code (lastName, postcode)}: every owner with the same last name (compared case-insensitively
 * with collapsed whitespace) and the same postcode belongs to the same household and so resolves to
 * the same deterministic {@code householdId}. {@link RejectDuplicateIdentity} uses this to detect
 * household duplicates and to fold the household component into a request's identity key, and
 * {@link AssignHousehold} uses it to stamp the computed id onto the owner, so both compute the same
 * value.
 */
final class Households {

    private Households() {
    }

    /**
     * The deterministic {@code householdId} for the given last name and postcode: the first 12 hex
     * characters of {@code SHA-256(normalizedLastName + '|' + postcode)}. It is computed for every
     * owner regardless of {@code sharesHousehold}, so owners with the same last name and postcode
     * share it automatically. Never {@code null}.
     */
    static String householdId(String lastName, String postcode) {
        String key = normalizeName(lastName) + "|" + normalizePostcode(postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-case, trim and collapse internal whitespace runs to a single space. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Trim the postcode; a {@code null} value contributes the empty string. */
    static String normalizePostcode(String value) {
        return value == null ? "" : value.trim();
    }
}
