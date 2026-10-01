package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;

/**
 * Single source of truth for an owner's {@code customerCode} and everything read back out of it.
 *
 * <p>The code is formatted {@code <REGION>-<HASH8>}:
 * <ul>
 * <li>{@code REGION} is the owner's region code, derived from the postcode (see
 * {@link OwnerRegion#localityOf}, which prefers the postcode and falls back to the city table), e.g.
 * {@code NSW}.</li>
 * <li>{@code HASH8} is the first eight upper-case hex characters of the SHA-256 of
 * {@code normalizedTelephone + lastName}, where {@code normalizedTelephone} is the E.164 telephone
 * (see {@link OwnerTelephone#toE164}).</li>
 * </ul>
 *
 * <p>There are no sequence numbers: the code depends only on the owner's own region, telephone and
 * last name, so it can be computed without coordinating through the database. Every other identity
 * value is derived from this code — the membership number and its Luhn check digit, the create audit
 * record and the read-time {@code locality} all read the region-and-hash code assigned here.
 */
public final class OwnerCustomerCode {

    private OwnerCustomerCode() {
    }

    /** The {@code <REGION>-<HASH8>} customer code for {@code owner}. */
    public static String customerCode(Owner owner) {
        String region = OwnerRegion.localityOf(owner.getCity(), owner.getPostcode());
        return region + "-" + hash8(owner.getTelephone(), owner.getLastName());
    }

    /**
     * The {@code HASH8} segment: the first eight upper-case hex characters of the SHA-256 of the
     * normalized (E.164) telephone concatenated with the last name.
     */
    static String hash8(String telephone, String lastName) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        String key = (normalizedTelephone == null ? "" : normalizedTelephone)
                + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
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
     * De-duplicates {@code base} against the {@code existing} customer codes: returns {@code base}
     * unchanged when it is not already taken, otherwise appends {@code -<n>} with the smallest
     * {@code n} of 2 or more that makes the code unique.
     */
    public static String deduplicate(String base, Set<String> existing) {
        if (!existing.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The {@code REGION} segment of a customer code (everything before the first {@code '-'}), or
     * {@code null} when there is no code to read it from.
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int separator = customerCode.indexOf('-');
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }
}
