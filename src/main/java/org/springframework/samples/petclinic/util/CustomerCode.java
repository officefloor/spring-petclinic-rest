package org.springframework.samples.petclinic.util;

import java.util.Collection;

/**
 * The owner's region-and-hash identity. The customerCode is formatted {@code <REGION>-<HASH8>} where
 * REGION is the region derived from the owner's postcode (see {@link Locality}) and HASH8 is the first
 * eight upper-case hex characters of SHA-256 over the normalized telephone concatenated with the last
 * name (see {@link Sha256}). The identity carries no per-owner sequence, so it does not depend on other
 * owners; the same telephone and last name always yield the same hash. Every value derived from the
 * customerCode — the membership number and its check digit, the create audit record and the locality —
 * therefore shares this single identity. Pure functions, so exposed as plain helpers.
 */
public final class CustomerCode {

    /** Number of hex characters in the hash portion of the code. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * Builds the customerCode {@code <region>-<HASH8>}. The hash is taken over the normalized telephone
     * followed by the last name; a {@code null} part contributes an empty string.
     */
    public static String of(String region, String normalizedTelephone, String lastName) {
        return region + "-" + Sha256.prefix(orEmpty(normalizedTelephone) + orEmpty(lastName), HASH_LENGTH);
    }

    /**
     * De-duplicates {@code candidate} against the {@code existing} customerCodes. Returns the candidate
     * unchanged when it does not collide; otherwise appends {@code -<n>} with the smallest {@code n} of 2
     * or more that yields a value absent from {@code existing}.
     */
    public static String dedupe(String candidate, Collection<String> existing) {
        if (!existing.contains(candidate)) {
            return candidate;
        }
        for (int n = 2; ; n++) {
            String deduped = candidate + "-" + n;
            if (!existing.contains(deduped)) {
                return deduped;
            }
        }
    }

    /** The region portion (before the first '-') of a customerCode, or {@code null} when absent. */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
