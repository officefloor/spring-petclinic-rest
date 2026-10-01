package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single source of truth for an owner's {@code memberId} and the values derived from it. The member id
 * unifies the former {@code customerCode} and {@code membershipNumber} into one identifier of the shape
 * {@code <REGION><FY><HASH8><CHK>}:
 *
 * <ul>
 * <li>{@code REGION} - the region code derived from the postcode (see
 * {@link org.springframework.samples.petclinic.mapper.Regions#regionForPostcode(String)}, e.g.
 * {@code NSW}, or {@code UNKNOWN} when the postcode resolves to no known region);</li>
 * <li>{@code FY} - the two-digit fiscal-year start year of the owner's (business-day-adjusted)
 * registration date (see {@link FiscalYear#twoDigitStartYear});</li>
 * <li>{@code HASH8} - the first 8 upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)} (see {@link #hash8(String, String)}), the same hash
 * used by the region-and-hash identity;</li>
 * <li>{@code CHK} - a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>The format is assembled by {@link #format(String, String, String)}, which embeds the region, fiscal
 * year, hash and check digit in that order with no separator. The region component itself is supplied by
 * the caller (under {@linkplain IdentityVersion identity version 2} it is the version-tagged region from
 * {@link org.springframework.samples.petclinic.mapper.Regions#regionForPostcodeV2(String)}); the
 * user-facing {@code locality}, {@code timezone} and owner segment are derived as the plain region from
 * the postcode (see {@link org.springframework.samples.petclinic.mapper.Regions#regionForPostcode(String)})
 * rather than read back out of the member id.
 */
public final class MemberId {

    /** The single character separating a de-duplication suffix from the base member id. */
    private static final char SEPARATOR = '-';

    /** The region string used when a postcode resolves to no known region. The canonical definition of
     *  this sentinel, which {@code Regions.UNKNOWN} also points at so the region a member id is built with
     *  and the region/locality derived elsewhere share one value. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    private MemberId() {
    }

    /**
     * Assemble a {@code <REGION><FY><HASH8><CHK>} member id from its components. The check digit is the
     * Luhn digit (see {@link #checkDigit(String)}) over the digits of the concatenated
     * {@code <REGION><FY><HASH8>} base.
     *
     * @param region    the region code (typically {@code Regions.regionForPostcode(...)})
     * @param fiscalYear the two-digit fiscal-year start year (see {@link FiscalYear#twoDigitStartYear})
     * @param hash8      the 8-character hash component (see {@link #hash8(String, String)})
     * @return the assembled member id
     */
    public static String format(String region, String fiscalYear, String hash8) {
        String base = region + fiscalYear + hash8;
        return base + checkDigit(base);
    }

    /**
     * Spell a de-duplicated member id by appending a numeric suffix to {@code base}, joined with the
     * {@link #SEPARATOR}. Used when a computed member id collides with an existing owner's: callers try
     * {@code n} of 2, 3, ... until the result is unique (e.g. {@code NSW261A2B3C4D7-2}).
     *
     * @param base the colliding base member id to disambiguate
     * @param n    the suffix number (2 or more)
     * @return {@code base}, the separator and {@code n}
     */
    public static String withSuffix(String base, int n) {
        return base + SEPARATOR + n;
    }

    /**
     * Compute the {@code HASH8} component: the first 8 upper-case hex characters of
     * {@code SHA-256(normalizedTelephone + lastName)}, where the telephone is normalized to E.164 form
     * (see {@link OwnerTelephone#toE164(String)}) exactly as the owner's identity key is, so the hash
     * over a submitted and a stored owner can never silently diverge.
     *
     * @param telephone the owner's telephone; normalized to E.164 before hashing
     * @param lastName  the owner's last name
     * @return the 8 upper-case hex characters
     */
    public static String hash8(String telephone, String lastName) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        if (normalizedTelephone == null) {
            normalizedTelephone = telephone == null ? "" : telephone;
        }
        String last = lastName == null ? "" : lastName;
        return sha256UpperHex(normalizedTelephone + last).substring(0, 8);
    }

    /**
     * Compute the Luhn check digit over the digits contained in {@code value}. An implementation detail
     * of {@link #format(String, String, String)}, which embeds it as the member id's {@code CHK}
     * segment; the digit is not otherwise exposed.
     *
     * @param value the value; non-digit characters are ignored
     * @return the Luhn check digit, between 0 and 9 inclusive
     */
    private static int checkDigit(String value) {
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

    /** Full upper-case hex SHA-256 of the UTF-8 bytes of {@code s}. */
    private static String sha256UpperHex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
