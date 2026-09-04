package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION
 * is the region derived from the owner's postcode (see {@link Locality}) and HASH8 is the
 * first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}.
 * The membership number, its check digit, the audit record and the locality all derive
 * from this identity; there is no per-city sequence any more.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The owner's {@code <REGION>-<HASH8>} customer code. */
    public static String assign(Owner owner) {
        return Locality.of(owner.getCity(), owner.getPostcode())
            + "-" + hash8(owner.getTelephone() + owner.getLastName());
    }

    /** The REGION component of a {@code <REGION>-<HASH8>} customer code. */
    public static String region(String customerCode) {
        return customerCode.substring(0, customerCode.indexOf('-'));
    }

    /** First 8 upper-case hex characters of SHA-256 over {@code value}. */
    private static String hash8(String value) {
        byte[] digest = sha256(value);
        StringBuilder hex = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            hex.append(String.format("%02X", digest[i]));
        }
        return hex.toString();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
