package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The unified owner {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} with no separators:
 *
 * <ul>
 *   <li>{@code REGION} — the region code derived from the postcode/city (see {@link OwnerRegion}),
 *       an upper-case letter run (NSW, VIC, QLD or UNKNOWN);</li>
 *   <li>{@code FY} — the 2-digit fiscal year (the last two digits of the fiscal year's start-year,
 *       the same {@code YY} as the owner's {@code fiscalYear});</li>
 *   <li>{@code HASH8} — the first 8 upper-case hex characters of SHA-256 over
 *       (normalizedTelephone + lastName), the same HASH8 used by the region-and-hash identity;</li>
 *   <li>{@code CHK} — a single Luhn check digit computed over the digits of
 *       {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>This replaces the former separate {@code customerCode}, {@code membershipNumber} and standalone
 * {@code checkDigit}: the region, fiscal year, audit record and owner segment now all read the
 * memberId. Collision de-duplication (appending {@code -<n>}) is applied to the whole memberId, so a
 * de-duplicated value may carry a trailing {@code -<n>} suffix; the parse helpers below tolerate it
 * by reading the region and fiscal year from the leading segments.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * Builds {@code <REGION><FY><HASH8><CHK>} from its parts, computing the Luhn check digit over the
     * digits of {@code <REGION><FY><HASH8>}.
     */
    public static String of(String region, String fy2, String hash8) {
        String base = region + fy2 + hash8;
        return base + luhnCheckDigit(base);
    }

    /**
     * The {@code REGION} segment of {@code memberId}: its leading run of upper-case letters (the FY
     * segment begins at the first digit). Returns {@code null} when {@code memberId} is null or has
     * no leading letters.
     */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? null : memberId.substring(0, i);
    }

    /**
     * The 2-digit {@code FY} segment of {@code memberId}, immediately after the region. Returns
     * {@code null} when {@code memberId} is null or too short to hold a fiscal year.
     */
    public static String fiscalYear2(String memberId) {
        String region = region(memberId);
        if (region == null) {
            return null;
        }
        int start = region.length();
        if (memberId.length() < start + 2) {
            return null;
        }
        return memberId.substring(start, start + 2);
    }

    /**
     * The single Luhn check digit (0-9) over the digits contained in {@code value}. Non-digit
     * characters are ignored; from the rightmost digit leftwards every second digit is doubled
     * (subtracting 9 when the result exceeds 9), and the check digit is {@code (10 - (sum % 10)) % 10}.
     */
    public static int luhnCheckDigit(String value) {
        if (value == null) {
            return 0;
        }
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
}
