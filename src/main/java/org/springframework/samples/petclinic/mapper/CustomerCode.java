package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds and reads an owner's {@code customerCode}, the region-and-hash identity
 * {@code '<REGION>-<HASH8>'}.
 *
 * <p>REGION is the canonical region for the owner (see {@link CityRegion}, postcode-preferred with a
 * city-table fallback). HASH8 is the first 8 UPPER-case hex characters of the SHA-256 digest over
 * {@code normalizedTelephone + lastName} (e.g. {@code 'NSW-1A2B3C4D'}). There are no sequence numbers.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The {@code '<REGION>-<HASH8>'} code for {@code region}, {@code normalizedTelephone} and {@code lastName}. */
    public static String of(String region, String normalizedTelephone, String lastName) {
        return region + "-" + hash8(normalizedTelephone, lastName);
    }

    /** First 8 UPPER-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}. */
    public static String hash8(String normalizedTelephone, String lastName) {
        String input = (normalizedTelephone == null ? "" : normalizedTelephone)
                + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * The owner's locality: the REGION part of its {@code customerCode} ({@code '<REGION>-<HASH8>'}).
     * When the owner has no customerCode (e.g. seed data not created through the endpoint), falls back
     * to deriving the region straight from postcode/city via {@link CityRegion}.
     */
    public static String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return CityRegion.locality(owner.getPostcode(), owner.getCity());
    }
}
