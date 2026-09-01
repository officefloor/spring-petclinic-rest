package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's unified member identifier '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;&lt;CHK&gt;': the region
 * code (derived from the postcode, else UNKNOWN), the two-digit fiscal year of the registration
 * date, the first eight upper-hex of SHA-256(normalizedTelephone + lastName) — the same HASH8 as the
 * region-and-hash identity — and a single Luhn check digit over the digits of the preceding segments.
 * Collision handling may append '-&lt;n&gt;'; the readers below ignore that suffix.
 */
public final class MemberId {

    private MemberId() {
    }

    /** The member id (without any collision suffix) for {@code owner}; requires a registration date. */
    public static String of(Owner owner) {
        String region = Locality.of(null, owner.getPostcode());
        String fy = String.format("%02d", Fiscal.startYear(owner.getRegistrationDate()) % 100);
        String hash8 = sha256Hex(orEmpty(owner.getTelephone()) + orEmpty(owner.getLastName()))
                .substring(0, 8).toUpperCase();
        String body = region + fy + hash8;
        return body + luhn(body);
    }

    /** The region segment of {@code memberId} (its leading run before FY+HASH8+CHK); null when absent. */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        String core = core(memberId);
        return core.substring(0, core.length() - 11);
    }

    /** The 'FY&lt;YY&gt;' fiscal-year label carried in {@code memberId}; null when absent. */
    public static String fiscalYear(String memberId) {
        if (memberId == null) {
            return null;
        }
        String core = core(memberId);
        return "FY" + core.substring(core.length() - 11, core.length() - 9);
    }

    private static String core(String memberId) {
        int dash = memberId.indexOf('-');
        return dash < 0 ? memberId : memberId.substring(0, dash);
    }

    /** Luhn check digit (0-9) over the digits contained in {@code value}. */
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

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
