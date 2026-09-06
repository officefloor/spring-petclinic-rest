package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds and interprets the owner {@code customerCode}, the single region-and-hash identity every
 * derived value (membership number, its check digit, the create audit line and the {@code locality})
 * now hangs off.
 *
 * <p>The code is {@code <REGION>-<HASH8>} where REGION is the {@link #region(Owner) region} derived
 * from the owner's postcode (falling back to the city, then {@code "UNKNOWN"}) and HASH8 is the first
 * eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}. Sequence
 * numbers are no longer part of the identity, so the code depends only on the owner's own fields.
 */
public final class CustomerCodes {

    /** Fixed city-to-region table (mirrors the read-only {@code locality} mapping). */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private CustomerCodes() {
    }

    /** Builds the {@code <REGION>-<HASH8>} customer code for the owner. */
    public static String build(Owner owner) {
        return region(owner) + "-" + hash8(owner.getTelephone(), owner.getLastName());
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

    /**
     * The REGION segment of a customer code (everything before the first {@code '-'}), or the whole
     * code when it contains no {@code '-'}. Returns {@code null} for a {@code null} code.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
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
