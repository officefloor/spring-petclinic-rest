package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code customerCode} - the single region-and-hash identity every other
 * derived value (membership number, check digit, locality, create audit line) is built from.
 *
 * <p>The code is formatted {@code <REGION>-<HASH8>} where REGION is the region derived from the
 * owner's postcode (via the shared {@link Locality} derivation, which prefers the postcode and
 * falls back to the city) and HASH8 is the first eight UPPER-case hex characters of SHA-256 over
 * the concatenation of the owner's normalized (E.164) telephone and last name. The per-city
 * sequence numbers of the old {@code <CITY3>-<LAST3>-<NNNN>} format are gone: the code no longer
 * depends on how many owners already exist, so it is a pure function of the owner's own fields.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * Builds the {@code <REGION>-<HASH8>} customer code for {@code owner}. The telephone is read
     * in its already-normalized E.164 form, so the hash matches the stored telephone.
     *
     * @param owner the owner whose customer code to build.
     * @return the region-and-hash customer code.
     */
    public static String of(Owner owner) {
        String region = Locality.of(owner.getPostcode(), owner.getCity());
        return region + "-" + hash8(owner.getTelephone(), owner.getLastName());
    }

    /**
     * The region segment of an owner's identity - the region encoded in the {@code customerCode}
     * when one has been assigned, otherwise the shared {@link Locality} derivation so owners
     * without a code (e.g. legacy seed data) still resolve a region.
     *
     * @param owner the owner whose region to derive.
     * @return the region, or {@code "UNKNOWN"} when it cannot be resolved.
     */
    public static String regionOf(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null || code.isBlank()) {
            return Locality.of(owner.getPostcode(), owner.getCity());
        }
        int dash = code.indexOf('-');
        return dash < 0 ? code : code.substring(0, dash);
    }

    /** First eight UPPER-case hex characters of SHA-256 over {@code telephone + lastName}. */
    private static String hash8(String telephone, String lastName) {
        String input = safe(telephone) + safe(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
