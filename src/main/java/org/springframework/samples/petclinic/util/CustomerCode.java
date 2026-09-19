package org.springframework.samples.petclinic.util;

import java.util.Collection;

/**
 * The owner customer-code format, {@code <REGION>-<HASH8>}: the canonical region (see
 * {@link CityLocality}) followed by the first eight upper-case hex characters of a
 * SHA-256 fingerprint. This class owns the format so the two concerns that depend on it
 * — composing the code at creation and reading the region back out for locality — stay
 * in step.
 */
public final class CustomerCode {

    private static final char SEPARATOR = '-';

    private CustomerCode() {
    }

    /**
     * Composes a customer code from its region and hash components.
     *
     * @param region the canonical region
     * @param hash8  the eight-character hash
     * @return the {@code <REGION>-<HASH8>} customer code
     */
    public static String of(String region, String hash8) {
        return region + SEPARATOR + hash8;
    }

    /**
     * De-duplicates a customer code against the codes already in use. If the code is
     * unused it is returned unchanged; otherwise {@code -<n>} is appended with the
     * smallest {@code n} of two or more that yields a code not already taken.
     *
     * @param customerCode the composed customer code
     * @param existing     the customer codes already in use
     * @return the {@code customerCode}, or a {@code <customerCode>-<n>} variant unique
     *         among {@code existing}
     */
    public static String deduplicate(String customerCode, Collection<String> existing) {
        String candidate = customerCode;
        for (int n = 2; existing.contains(candidate); n++) {
            candidate = customerCode + SEPARATOR + n;
        }
        return candidate;
    }

    /**
     * The region component of a customer code, i.e. the part before the first separator.
     *
     * @param customerCode a customer code, or {@code null}
     * @return the region, or {@code null} when the code is absent or has no region part
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        return separator > 0 ? customerCode.substring(0, separator) : null;
    }
}
