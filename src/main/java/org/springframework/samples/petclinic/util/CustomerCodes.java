package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code customerCode} as {@code '<REGION>-<HASH8>'}: the region code
 * derived from the owner's postcode joined to the first 8 upper-case hex characters of
 * SHA-256 over {@code (normalizedTelephone + lastName)}. The region segment also drives
 * the owner's locality, so both read from the same identity.
 */
public final class CustomerCodes {

    private CustomerCodes() {
    }

    /** The {@code '<REGION>-<HASH8>'} customer code for the given owner. */
    public static String of(Owner owner) {
        return Localities.regionOf(owner.getCity(), owner.getPostcode()) + "-"
            + hash8(owner.getTelephone() + owner.getLastName());
    }

    /** The region segment of a customer code, or {@code "UNKNOWN"} when it is absent. */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String hash8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
