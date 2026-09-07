package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's customer code — the single derived identity every other value hangs off. It is
 * {@code '<REGION>-<HASH8>'}: REGION is the canonical region derived from the owner's postcode
 * (falling back to the city table via {@link Locality}); HASH8 is the first 8 UPPER-case hex
 * characters of SHA-256 over the normalized (E.164) telephone concatenated with the last name.
 *
 * <p>There are no sequence numbers: the code is a pure function of region, telephone and last
 * name, so it needs no repository scan and is stable for a given owner. The membership number,
 * its Luhn check digit ({@link CheckDigit}), the create audit line and the reported locality
 * ({@link #region(String)}) are all built from this one value.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The {@code '<REGION>-<HASH8>'} customer code for {@code owner}. */
    public static String of(Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String hash8 = sha256Hex(owner.getTelephone() + owner.getLastName())
                .substring(0, 8).toUpperCase(Locale.ROOT);
        return region + "-" + hash8;
    }

    /**
     * The REGION component of a customer code (everything before the final {@code '-'}), or
     * {@code null} when the code is absent. This is the owner's locality, now read straight off
     * the identity rather than re-derived.
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int hyphen = customerCode.lastIndexOf('-');
        return hyphen < 0 ? customerCode : customerCode.substring(0, hyphen);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
