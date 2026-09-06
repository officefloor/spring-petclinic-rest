package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds the owner {@code memberId}, the single unified region-and-hash identity every derived value
 * (the create audit line, the {@code locality} and {@code ownerSegment}) now hangs off. It replaces
 * the former separate {@code customerCode} and {@code membershipNumber}.
 *
 * <p>The id is {@code <REGION><FY><HASH8><CHK>} (no separators) where:
 * <ul>
 * <li>REGION is the {@link #region(Owner) region} derived from the owner's postcode (falling back to
 * the city, then {@code "UNKNOWN"});</li>
 * <li>FY is the two-digit fiscal year (starting 1 July) containing the {@code registrationDate};</li>
 * <li>HASH8 is the first eight upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)};</li>
 * <li>CHK is a single {@link Luhn#checkDigit(String) Luhn check digit} (0-9) computed over the digits
 * of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * The identity depends only on the owner's own fields; there are no sequence numbers.
 */
public final class MemberIds {

    /** Fixed city-to-region table (mirrors the read-only {@code locality} mapping). */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private MemberIds() {
    }

    /**
     * Builds the {@code <REGION><FY><HASH8><CHK>} memberId for the owner (e.g. 'NSW271A2B3C4D5').
     * Returns {@code null} when the registration date is absent (e.g. legacy seed owners), since the
     * FY segment cannot be formed without it.
     */
    public static String build(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        String base = region(owner) + fiscalYear(owner) + hash8(owner);
        return base + Luhn.checkDigit(base);
    }

    /**
     * First eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)} for the
     * owner — the HASH8 segment of the region-and-hash identity, independent of how that identity is
     * formatted.
     */
    public static String hash8(Owner owner) {
        return hash8(owner.getTelephone(), owner.getLastName());
    }

    /**
     * The region for the owner: the region whose inclusive postcode range contains the owner's
     * postcode wins; otherwise the fixed city-to-region table ({@code Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD}); otherwise {@code "UNKNOWN"}.
     */
    public static String region(Owner owner) {
        String region = regionFromPostcode(owner.getPostcode());
        if (region != null) {
            return region;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /** The two-digit fiscal-year (FY) segment of the memberId, e.g. "27" for a FY ending in 2027. */
    private static String fiscalYear(Owner owner) {
        return String.format("%02d", FiscalYears.of(owner.getRegistrationDate()) % 100);
    }

    /** First eight upper-case hex characters of {@code SHA-256(telephone + lastName)}. */
    private static String hash8(String telephone, String lastName) {
        String input = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
        byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(16);
        for (int i = 0; i < 4; i++) {
            hex.append(String.format("%02X", bytes[i]));
        }
        return hex.toString();
    }

    /**
     * The region whose inclusive postcode range contains the given 4-digit postcode, or
     * {@code null} when the postcode is absent, non-numeric, or in no known range.
     */
    private static String regionFromPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
