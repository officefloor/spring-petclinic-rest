package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * The owner customer-code identity, formatted {@code <REGION>-<HASH8>}: the region code
 * derived from the postcode, then the first eight upper-case hex characters of the SHA-256
 * digest of the owner's normalized telephone followed by their last name. Every value built
 * from the identity (membership number, check digit, locality) reads it back through here, so
 * the format lives in one place.
 */
public final class CustomerCode {

    private static final char SEPARATOR = '-';

    /** Number of leading hex characters of the digest kept as the identity's hash component. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * Build the customer code {@code <region>-<HASH8>} for an owner in {@code region} whose
     * normalized telephone and last name hash to {@code HASH8}.
     */
    public static String of(String region, String normalizedTelephone, String lastName) {
        String hash8 = Sha256.hex(normalizedTelephone + lastName)
                .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        return region + SEPARATOR + hash8;
    }

    /**
     * De-duplicate {@code customerCode} against the codes already {@code taken}. When it is free
     * the code is returned unchanged; otherwise {@code -<n>} is appended with the smallest
     * {@code n} of 2 or more that yields a code no owner already holds.
     */
    public static String deduplicate(String customerCode, Set<String> taken) {
        if (!taken.contains(customerCode)) {
            return customerCode;
        }
        for (int n = 2; ; n++) {
            String candidate = customerCode + SEPARATOR + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The region component of {@code customerCode} (its identity locality), or
     * {@link CityRegions#UNKNOWN} when the code is absent or carries no region.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return CityRegions.UNKNOWN;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        String region = separator > 0 ? customerCode.substring(0, separator) : "";
        return region.isEmpty() ? CityRegions.UNKNOWN : region;
    }
}
