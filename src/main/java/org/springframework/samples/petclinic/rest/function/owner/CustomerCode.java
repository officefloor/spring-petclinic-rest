package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        byte[] digest = sha256(telephone + lastName);
        StringBuilder hex = new StringBuilder(HASH_LENGTH);
        for (int i = 0; hex.length() < HASH_LENGTH; i++) {
            hex.append(String.format("%02X", digest[i]));
        }
        return hex.substring(0, HASH_LENGTH);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
