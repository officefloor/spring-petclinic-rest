package org.springframework.samples.petclinic.rest.function.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds the owner's {@code customerCode}, the single region-and-hash identity every other derived
 * value hangs off. The code is {@code '<REGION>-<HASH8>'} where {@code REGION} is the region derived
 * from the postcode (falling back to the city, see {@link Localities}) and {@code HASH8} is the first
 * eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}.
 *
 * <p>The telephone is already normalized to E.164 by the time an owner is built, so the hash is stable
 * for a given owner regardless of how the telephone was typed. Because the region prefix is exactly the
 * owner's locality, {@link #localityOf(Owner)} reads it straight back off the code.
 */
public final class CustomerCodes {

    private CustomerCodes() {
    }

    /** The {@code '<REGION>-<HASH8>'} customer code for {@code owner}. */
    public static String of(Owner owner) {
        return Localities.of(owner.getPostcode(), owner.getCity()) + "-"
                + hash8(owner.getTelephone(), owner.getLastName());
    }

    /**
     * De-duplicates {@code code} against {@code taken}: if no existing owner already uses it the code
     * is returned unchanged, otherwise {@code '-<n>'} is appended with the smallest {@code n >= 2}
     * that makes the result unique (e.g. {@code 'NSW-1A2B3C4D-2'}).
     */
    public static String deduplicate(String code, java.util.Collection<String> taken) {
        if (!taken.contains(code)) {
            return code;
        }
        for (int n = 2; ; n++) {
            String candidate = code + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The first eight upper-case hex characters of {@code SHA-256(telephone + lastName)}, over the
     * UTF-8 bytes of the concatenation. A {@code null} component contributes nothing.
     */
    public static String hash8(String telephone, String lastName) {
        String input = safe(telephone) + safe(lastName);
        byte[] digest = sha256(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    /**
     * The owner's locality: the {@code REGION} prefix of its customer code, or, when the code has not
     * been assigned yet, the region derived directly from the postcode and city.
     */
    public static String localityOf(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return Localities.of(owner.getPostcode(), owner.getCity());
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
