package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

/**
 * Builds and derives values from an owner's {@code memberId}, the single unified member identifier.
 * The id is {@code '<REGION><FY><HASH8><CHK>'} where:
 * <ul>
 * <li>REGION is the region derived from the owner's postcode/city (see {@link Locality}),</li>
 * <li>FY is the two-digit fiscal year of the registration date (see {@link FiscalYear}),</li>
 * <li>HASH8 is the first eight upper-case hex characters of SHA-256 over the
 * {@code normalizedTelephone + lastName} (the same hash used by the region-and-hash identity), and</li>
 * <li>CHK is a single Luhn check digit computed over the decimal digits of
 * {@code <REGION><FY><HASH8>} (non-digit characters such as the region and hex letters are ignored),
 * doubling every second digit from the right.</li>
 * </ul>
 * For example 'NSW271A2B3C4D5'.
 */
public final class MemberId {

    private MemberId() {
    }

    /** Assemble the {@code '<REGION><FY><HASH8><CHK>'} member id for the given region, registration
     *  date, telephone and last name. */
    public static String of(String region, LocalDate registrationDate, String telephone, String lastName) {
        String fy = String.format("%02d", FiscalYear.of(registrationDate) % 100);
        String base = region + fy + hash8(telephone, lastName);
        return base + checkDigit(base);
    }

    /** First eight UPPER-case hex characters of SHA-256 over {@code (telephone + lastName)}. */
    public static String hash8(String telephone, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String name = lastName == null ? "" : lastName;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest((tel + name).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** The single Luhn check digit (0-9) over the decimal digits contained in {@code code}. */
    public static int checkDigit(String code) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
