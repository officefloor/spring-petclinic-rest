package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single source of truth for an owner's {@code customerCode} and the values derived from it.
 *
 * <p>The code has the shape {@code <REGION>-<HASH8>}: {@code REGION} is the region code derived from
 * the postcode and {@code HASH8} is the first 8 upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)} (see {@link #hash8(String, String)}). The format is
 * assembled by {@link #format(String, String)} and read back by {@link #region(String)}; both sit here
 * so the separator and ordering live as one matched pair. Sequence numbers are no longer part of the
 * identity.
 *
 * <p>The {@link #checkDigit(String)} rule computes the standard Luhn check digit over the digits
 * contained in the code (non-digit characters, such as the formatting dash, are ignored). It is the
 * single source of truth for the {@code checkDigit} response field exposed by {@code OwnerMapper}.
 */
public final class CustomerCode {

    /** The single character separating the {@code REGION} and {@code HASH8} components. */
    private static final char SEPARATOR = '-';

    /** The region string used when a postcode (or a stored code) resolves to no known region. The
     *  canonical definition of this sentinel, which {@code Regions.UNKNOWN} also points at so the
     *  region a code is built with and the region read back out can never diverge. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    private CustomerCode() {
    }

    /**
     * Assemble a {@code <REGION>-<HASH8>} customer code from its components. The exact inverse of
     * {@link #region(String)}: both use the one {@link #SEPARATOR} defined here, so the producing and
     * parsing sides cannot drift apart.
     *
     * @param region the region code (typically {@code Regions.regionForPostcode(...)})
     * @param hash8  the 8-character hash component (see {@link #hash8(String, String)})
     * @return the assembled customer code
     */
    public static String format(String region, String hash8) {
        return region + SEPARATOR + hash8;
    }

    /**
     * Compute the {@code HASH8} component: the first 8 upper-case hex characters of
     * {@code SHA-256(normalizedTelephone + lastName)}, where the telephone is normalized to E.164 form
     * (see {@link OwnerTelephone#toE164(String)}) exactly as the owner's identity key is, so the hash
     * over a submitted and a stored owner can never silently diverge.
     *
     * @param telephone the owner's telephone; normalized to E.164 before hashing
     * @param lastName  the owner's last name
     * @return the 8 upper-case hex characters
     */
    public static String hash8(String telephone, String lastName) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        if (normalizedTelephone == null) {
            normalizedTelephone = telephone == null ? "" : telephone;
        }
        String last = lastName == null ? "" : lastName;
        return sha256UpperHex(normalizedTelephone + last).substring(0, 8);
    }

    /**
     * Read the {@code REGION} component back out of a {@code <REGION>-<HASH8>} customer code; the exact
     * inverse of {@link #format(String, String)}. This is the single source of truth for the
     * {@code locality} response field, so the locality an owner reports is exactly the region baked into
     * its identity.
     *
     * @param code the customer code; may be {@code null}
     * @return the region prefix, or {@link #UNKNOWN_REGION} when the code is {@code null} or has no dash
     */
    public static String region(String code) {
        if (code == null) {
            return UNKNOWN_REGION;
        }
        int dash = code.indexOf(SEPARATOR);
        return dash < 0 ? code : code.substring(0, dash);
    }

    /**
     * Compute the Luhn check digit over the digits contained in {@code code}.
     *
     * @param code the customer code; non-digit characters are ignored
     * @return the Luhn check digit, between 0 and 9 inclusive
     */
    public static int checkDigit(String code) {
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /** Full upper-case hex SHA-256 of the UTF-8 bytes of {@code s}. */
    private static String sha256UpperHex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
