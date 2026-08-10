package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Builds and derives values from an owner's {@code customerCode}. The code is
 * {@code '<REGION>-<HASH8>'} where REGION is the region derived from the owner's postcode/city
 * (see {@link Locality}) and HASH8 is the first eight upper-case hex characters of SHA-256 over
 * the {@code normalizedTelephone + lastName}. The {@code checkDigit} is a single Luhn check digit
 * computed over the decimal digits contained in the code (non-digit characters such as the '-'
 * separator or the hex letters are ignored), doubling every second digit from the right. This is
 * the standard Luhn algorithm restated over the code's digits.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** Assemble the {@code '<REGION>-<HASH8>'} code for the given region, telephone and last name. */
    public static String of(String region, String telephone, String lastName) {
        return region + "-" + hash8(telephone, lastName);
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

    /** The REGION portion of a {@code '<REGION>-<HASH8>'} code. */
    public static String region(String code) {
        int dash = code.indexOf('-');
        return dash < 0 ? code : code.substring(0, dash);
    }

    /** The Luhn check digit (0-9) over the decimal digits contained in {@code code}. */
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
