package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;

/**
 * Derives an owner's {@code customerCode} — the owner's region-and-hash identity,
 * formatted {@code <REGION>-<HASH8>}:
 *
 * <ul>
 * <li>{@code REGION} is the region code derived from the postcode: look the postcode up
 * against the inclusive region ranges (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) and,
 * only when the postcode is absent or in no known range, fall back to the fixed
 * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), else
 * {@code UNKNOWN}.</li>
 * <li>{@code HASH8} is the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName}, where the telephone is normalized to E.164 form
 * (an absent or unparseable telephone contributes an empty string).</li>
 * </ul>
 *
 * <p>The whole owner identity — the {@code customerCode} itself, the values built from it
 * (membership number and its Luhn check digit), the create audit record and the derived
 * locality — flows from this single value. There is no sequence number.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class OwnerCustomerCode {

    /** City -> canonical region, matching the fixed table used to validate postcodes. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerCustomerCode() {
    }

    /** The full {@code <REGION>-<HASH8>} customer code for the given owner fields. */
    public static String of(String postcode, String city, String telephone, String lastName) {
        return region(postcode, city) + "-" + hash8(telephone, lastName);
    }

    /**
     * The region code derived from the postcode (range lookup), falling back to the fixed
     * city-to-region table when the postcode is absent or in no known range, else
     * {@code UNKNOWN}.
     */
    public static String region(String postcode, String city) {
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ex) {
                // not a numeric postcode; fall back to the city table
            }
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone +
     * lastName}, where the telephone is normalized to E.164 (an absent or unparseable
     * telephone, or an absent last name, contributes an empty string).
     */
    public static String hash8(String telephone, String lastName) {
        String tel = E164Telephone.normalizeOrNull(telephone);
        String normalizedTelephone = tel == null ? "" : tel;
        String last = lastName == null ? "" : lastName;
        return sha256Hex(normalizedTelephone + last).substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
