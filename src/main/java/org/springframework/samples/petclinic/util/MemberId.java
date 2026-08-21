package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code memberId} - the single unified identity that replaces the former
 * customerCode, membershipNumber and checkDigit, and that every other derived value (locality,
 * create audit line) is built from.
 *
 * <p>The id is formatted {@code <REGION><FY><HASH8><CHK>} (no separators) where:
 * <ul>
 * <li>REGION is the region derived from the owner's postcode (via the shared {@link Locality}
 * derivation, which prefers the postcode and falls back to the city);</li>
 * <li>FY is the two-digit fiscal year (fiscal year starts 1 July) of the business-day-adjusted
 * registrationDate;</li>
 * <li>HASH8 is the first eight UPPER-case hex characters of SHA-256 over the concatenation of the
 * owner's normalized (E.164) telephone and last name - the same HASH8 used by the region-and-hash
 * identity;</li>
 * <li>CHK is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * The id is a pure function of the owner's own fields, so it does not depend on how many owners
 * already exist.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * Builds the {@code <REGION><FY><HASH8><CHK>} member id for {@code owner}. The telephone is read
     * in its already-normalized E.164 form, so the hash matches the stored telephone.
     *
     * @param owner the owner whose member id to build.
     * @return the unified member id.
     */
    public static String of(Owner owner) {
        String region = Locality.of(owner.getPostcode(), owner.getCity());
        String fy = String.format("%02d", FiscalYear.yearSegment(owner.getRegistrationDate()));
        String hash8 = hash8(owner.getTelephone(), owner.getLastName());
        String base = region + fy + hash8;
        return base + CheckDigit.luhn(base);
    }

    /**
     * The region segment of an owner's identity - the shared {@link Locality} derivation (postcode
     * preferred, city fallback), so it matches the region encoded in the {@code memberId} and the
     * reported {@code locality}.
     *
     * @param owner the owner whose region to derive.
     * @return the region, or {@code "UNKNOWN"} when it cannot be resolved.
     */
    public static String regionOf(Owner owner) {
        return Locality.of(owner.getPostcode(), owner.getCity());
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
