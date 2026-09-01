package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Builds an owner's unified memberId as {@code <REGION><FY><HASH8><CHK>}: REGION is the
 * region derived from the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099,
 * otherwise "UNKNOWN"), FY the two-digit registrationDate fiscal year, HASH8 the first 8
 * upper-case hex characters of SHA-256(normalizedTelephone + lastName), and CHK a single
 * Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The memberId for {@code owner} (before any collision suffix). */
    public static String of(Owner owner) {
        String base = region(owner)
            + String.format("%02d", FiscalYear.startYearOf(owner.getRegistrationDate()) % 100)
            + hash8(owner);
        return base + luhn(base);
    }

    /** The region derived from {@code owner}'s postcode, or "UNKNOWN". */
    public static String region(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("\\d{4}")) {
            return switch (Integer.parseInt(postcode) / 100) {
                case 20 -> "NSW";
                case 30 -> "VIC";
                case 40 -> "QLD";
                default -> "UNKNOWN";
            };
        }
        return "UNKNOWN";
    }

    private static String hash8(Owner owner) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest((owner.getTelephone() + owner.getLastName()).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        return HexFormat.of().withUpperCase().formatHex(digest).substring(0, 8);
    }

    private static int luhn(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
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
}
