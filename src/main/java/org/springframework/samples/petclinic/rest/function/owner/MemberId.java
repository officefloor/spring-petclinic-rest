package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.samples.petclinic.mapper.OwnerMapper;

/**
 * Single source of truth for an owner's {@code memberId}, the unified identity
 * {@code '<REGION><FY><HASH8><CHK>'} where:
 *
 * <ul>
 *   <li>{@code REGION} is the region code derived from the postcode range (NSW 2000-2099,
 *       VIC 3000-3099, QLD 4000-4099), falling back to the city table (Sydney→NSW, Melbourne→VIC,
 *       Brisbane→QLD) and finally {@code 'UNKNOWN'};</li>
 *   <li>{@code FY} is the two-digit fiscal year (the last two digits of the fiscal year, starting
 *       1 July, that contains the registrationDate — the same year segment as
 *       {@link org.springframework.samples.petclinic.mapper.OwnerMapper#fiscalYear});</li>
 *   <li>{@code HASH8} is the first 8 upper-case hex characters of the SHA-256 of
 *       {@code normalizedTelephone + lastName} (the same HASH8 used by the region-and-hash
 *       identity); and</li>
 *   <li>{@code CHK} is a single Luhn check digit (0-9) computed over the digits of
 *       {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>There is no per-city sequence: two owners with the same normalized telephone, last name,
 * region and fiscal year derive the same memberId. Everything downstream of the identity — the
 * create audit line and the {@code locality} — is derived from the same region (see
 * {@link org.springframework.samples.petclinic.mapper.OwnerMapper}).
 */
public final class MemberId {

    /** Fixed city-to-region table backing the {@code REGION} fallback when the postcode resolves none. */
    private static final Map<String, String> CITY_REGIONS = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private MemberId() {
    }

    /**
     * The full {@code '<REGION><FY><HASH8><CHK>'} memberId for the given normalized telephone, last
     * name, postcode, city and registrationDate.
     */
    public static String of(String normalizedTelephone, String lastName, String postcode, String city,
            LocalDate registrationDate) {
        String base = region(postcode, city) + fiscalYearSegment(registrationDate)
                + hash8(normalizedTelephone, lastName);
        return base + luhn(base);
    }

    /**
     * Derives {@code REGION}: the postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099),
     * then the {@link #CITY_REGIONS} city table, then {@code 'UNKNOWN'}. This disambiguates cities that
     * share a name and is the region embedded in every {@code memberId}.
     */
    public static String region(String postcode, String city) {
        String region = regionForPostcode(postcode);
        if (region != null) {
            return region;
        }
        return CITY_REGIONS.getOrDefault(city, "UNKNOWN");
    }

    /** {@code HASH8}: the first 8 upper-case hex characters of SHA-256 of {@code normalizedTelephone + lastName}. */
    public static String hash8(String normalizedTelephone, String lastName) {
        String tel = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        return shaHex(tel + last, 8);
    }

    /** The two-digit fiscal-year segment for {@code registrationDate}, or {@code "00"} when absent. */
    private static String fiscalYearSegment(LocalDate registrationDate) {
        if (registrationDate == null) {
            return "00";
        }
        return String.format("%02d", OwnerMapper.fiscalYearEnding(registrationDate) % 100);
    }

    /** Single Luhn check digit (0-9) over the digits contained in {@code value}. */
    private static int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
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
