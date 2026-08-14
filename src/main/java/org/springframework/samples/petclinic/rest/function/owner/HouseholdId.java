package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Pipeline variable carrying the deterministic shared household identifier from
 * {@link DeriveHouseholdId} to {@link EnsureUniqueIdentity} and {@link AssignHousehold}. A dedicated
 * type (rather than a raw {@code String}) keeps the OfficeFloor by-type variable matching
 * unambiguous without a qualifier.
 *
 * <p>The identifier is <em>computed</em>, not allocated: {@link #compute} derives it from the
 * owner's normalized lastName and postcode, so owners with the same lastName and postcode share it
 * automatically without any owner opting in.
 */
public final class HouseholdId {

    private final String value;

    public HouseholdId(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    /**
     * The deterministic household identifier: the first 12 hex characters of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}. Owners with the same lastName and postcode
     * therefore always share it.
     *
     * <p>A household is keyed on {@code (lastName, postcode)}, so without a postcode there is no
     * shared household: an absent or blank postcode yields {@code null} (a household of one).
     */
    public static String compute(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String seed = normalizeLastName(lastName) + "|" + postcode.trim();
        return sha256Hex12(seed);
    }

    /** Trim, collapse internal whitespace runs to a single space and lower-case. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** The first 12 hex characters of SHA-256 over {@code seed}. */
    private static String sha256Hex12(String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(12);
            for (int i = 0; hex.length() < 12; i++) {
                hex.append(String.format("%02x", hash[i] & 0xff));
            }
            return hex.substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e); // every JRE ships SHA-256
        }
    }
}
