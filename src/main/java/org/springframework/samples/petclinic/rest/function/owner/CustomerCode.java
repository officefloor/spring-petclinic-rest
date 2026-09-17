package org.springframework.samples.petclinic.rest.function.owner;

import java.util.function.Predicate;

/**
 * The single definition of an owner's customer code: {@code <REGION>-<HASH8>}, where REGION
 * is the region derived from the owner's postcode (see {@link Locality}) and HASH8 is the
 * first 8 upper-case hex characters of SHA-256 over the owner's normalized telephone
 * followed by its last name. The code carries no sequence number and so is stable for a
 * given identity.
 *
 * <p>Formatting only; the region and identity fields are supplied by the caller
 * ({@link AssignOwnerCustomerCode} derives the region and reads the stored fields).
 */
public final class CustomerCode {

    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * Format {@code region} and the identity fields into a customer code, e.g.
     * {@code format("NSW", "+61412345678", "Smithers")} yields {@code "NSW-1A2B3C4D"} (the
     * hash segment being the first 8 upper-case hex digits of SHA-256 over
     * {@code telephone + lastName}).
     */
    public static String format(String region, String telephone, String lastName) {
        return region + "-" + hash8(telephone, lastName);
    }

    /**
     * De-duplicate {@code code} against codes already in use. If {@code code} is free it is
     * returned unchanged; otherwise {@code -<n>} is appended with the smallest {@code n >= 2}
     * that yields an unused code, e.g. a third collision on {@code "NSW-1A2B3C4D"} becomes
     * {@code "NSW-1A2B3C4D-3"}. The suffix leaves {@link #region(String)} unchanged.
     *
     * @param code  the freshly formatted code
     * @param inUse tests whether a candidate code is already taken
     */
    public static String deduplicate(String code, Predicate<String> inUse) {
        if (!inUse.test(code)) {
            return code;
        }
        for (int n = 2; ; n++) {
            String candidate = code + "-" + n;
            if (!inUse.test(candidate)) {
                return candidate;
            }
        }
    }

    /** The region segment of {@code customerCode} (the text before the first {@code '-'}), or
     *  {@code null} when the code is absent. */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    /** The first 8 upper-case hex characters of SHA-256 over {@code telephone + lastName}. */
    private static String hash8(String telephone, String lastName) {
        return Sha256.hex(telephone + lastName, HASH_LENGTH);
    }
}
