package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * The owner's customer-code identity, formatted {@code <REGION>-<HASH8>} where REGION is
 * the owner's region (see {@link OwnerRegion}) and HASH8 is the first 8 upper-case hex
 * characters of SHA-256 over the normalized telephone concatenated with the last name
 * (e.g. {@code NSW-1A2B3C4D}). This is the single source of an owner's identity: the
 * membership number, its check digit and the audit record are all built from it, and the
 * locality is the REGION read back out of it.
 */
public final class CustomerCode {

    /** Separator between the REGION and HASH8 segments. */
    private static final String SEPARATOR = "-";

    /** Number of leading hex characters of the digest kept as the HASH8 segment. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * The {@code <REGION>-<HASH8>} customer code for the given region and identity inputs.
     *
     * @param region    the owner's region, used verbatim as the REGION segment
     * @param telephone the owner's normalized (E.164) telephone
     * @param lastName  the owner's last name
     */
    public static String of(String region, String telephone, String lastName) {
        return region + SEPARATOR + hash8(telephone, lastName);
    }

    /** The REGION segment of a customer code: everything before the first separator. */
    public static String regionOf(String customerCode) {
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }

    /** First {@value #HASH_LENGTH} upper-case hex characters of SHA-256(telephone + lastName). */
    private static String hash8(String telephone, String lastName) {
        return Sha256.hex(telephone + lastName).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
    }
}
