package org.springframework.samples.petclinic.model;

import java.util.function.Predicate;

/**
 * The owner customer code identity, formatted {@code <REGION>-<HASH8>}: REGION is the region code
 * derived from the owner's postcode (falling back to its city, else {@link CityRegion#UNKNOWN}) and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalized telephone followed by
 * the last name (e.g. {@code NSW-1A2B3C4D}).
 *
 * <p>It is the single identity every downstream value is derived from: the membership number and its
 * check digit are built from the whole code, and the owner's {@link Owner#getLocality() locality} is
 * the REGION read back out of it.
 */
public final class CustomerCode {

    /** Number of leading hex characters of the hash kept in the code. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * The customer code for an owner in {@code region} whose normalized telephone and last name are
     * {@code telephone} and {@code lastName}.
     */
    public static String of(String region, String telephone, String lastName) {
        return region + "-" + Sha256.hex(telephone + lastName).substring(0, HASH_LENGTH);
    }

    /**
     * A unique variant of {@code base}: {@code base} itself when {@code taken} reports it free,
     * otherwise {@code base-<n>} with the smallest {@code n >= 2} that {@code taken} reports free.
     * Used to de-duplicate a derived code against the codes already in use.
     */
    public static String deduplicate(String base, Predicate<String> taken) {
        if (!taken.test(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The REGION portion of {@code customerCode} — everything before the first {@code '-'} — or
     * {@link CityRegion#UNKNOWN} when the code is absent or carries no region.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return CityRegion.UNKNOWN;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? CityRegion.UNKNOWN : customerCode.substring(0, dash);
    }
}
