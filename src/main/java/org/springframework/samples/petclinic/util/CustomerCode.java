package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * The owner's customer code, {@code '<REGION>-<HASH8>'}: the owner's {@link OwnerRegion region}
 * joined to an 8-character hash that identifies the person. HASH8 is the first 8 upper-case hex
 * characters of {@code SHA-256(normalizedTelephone + lastName)}, so the same person (same
 * normalized telephone and last name) always resolves to the same code without any sequence
 * number (e.g. {@code 'NSW-1A2B3C4D'}).
 */
public final class CustomerCode {

    private static final String SEPARATOR = "-";

    /** Number of leading hex characters of the digest kept as HASH8. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * The customer code for the given {@code region} and person, hashing the person's
     * {@code normalizedTelephone} and {@code lastName}.
     */
    public static String of(String region, String normalizedTelephone, String lastName) {
        return region + SEPARATOR + hash8(orEmpty(normalizedTelephone) + orEmpty(lastName));
    }

    /** The REGION segment of a customer code (the text before the first {@code '-'}). */
    public static String region(String customerCode) {
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }

    private static String hash8(String source) {
        return Sha256.hex(source).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
