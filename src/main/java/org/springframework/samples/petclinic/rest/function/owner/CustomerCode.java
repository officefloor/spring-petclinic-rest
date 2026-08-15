package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Single source of truth for an owner's {@code customerCode}, the region-and-hash identity
 * {@code '<REGION>-<HASH8>'} where:
 *
 * <ul>
 *   <li>{@code REGION} is the region code derived from the postcode range (NSW 2000-2099,
 *       VIC 3000-3099, QLD 4000-4099), falling back to the city table (Sydney→NSW, Melbourne→VIC,
 *       Brisbane→QLD) and finally {@code 'UNKNOWN'}; and</li>
 *   <li>{@code HASH8} is the first 8 upper-case hex characters of the SHA-256 of
 *       {@code normalizedTelephone + lastName}.</li>
 * </ul>
 *
 * <p>There is no per-city sequence: two owners with the same normalized telephone and last name in
 * the same region derive the same code. Everything downstream of the identity — the membership number
 * and its Luhn check digit, the create audit line and the {@code locality} — is derived from this
 * code (see {@link org.springframework.samples.petclinic.mapper.OwnerMapper}).
 */
public final class CustomerCode {

    /** Fixed city-to-region table backing the {@code REGION} fallback when the postcode resolves none. */
    private static final Map<String, String> CITY_REGIONS = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CustomerCode() {
    }

    /**
     * The full {@code '<REGION>-<HASH8>'} code for the given normalized telephone, last name, postcode
     * and city.
     */
    public static String of(String normalizedTelephone, String lastName, String postcode, String city) {
        return region(postcode, city) + "-" + hash8(normalizedTelephone, lastName);
    }

    /**
     * Derives {@code REGION}: the postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099),
     * then the {@link #CITY_REGIONS} city table, then {@code 'UNKNOWN'}. This disambiguates cities that
     * share a name and is the region embedded in every {@code customerCode}.
     */
    public static String region(String postcode, String city) {
        String region = regionForPostcode(postcode);
        if (region != null) {
            return region;
        }
        return CITY_REGIONS.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The {@code REGION} prefix carried by a {@code customerCode} of the form {@code '<REGION>-<HASH8>'},
     * or {@code null} when the code is absent or malformed.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash > 0 ? customerCode.substring(0, dash) : null;
    }

    /** {@code HASH8}: the first 8 upper-case hex characters of SHA-256 of {@code normalizedTelephone + lastName}. */
    public static String hash8(String normalizedTelephone, String lastName) {
        String tel = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        return shaHex(tel + last, 8);
    }

    /**
     * Derives the region from a 4-digit postcode: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
     * Returns {@code null} when the postcode is absent, non-numeric or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int code;
        try {
            code = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        if (code >= 2000 && code <= 2099) {
            return "NSW";
        }
        if (code >= 3000 && code <= 3099) {
            return "VIC";
        }
        if (code >= 4000 && code <= 4099) {
            return "QLD";
        }
        return null;
    }

    /** First {@code n} upper-case hex characters of SHA-256 of {@code value}. */
    private static String shaHex(String value, int n) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, n);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
