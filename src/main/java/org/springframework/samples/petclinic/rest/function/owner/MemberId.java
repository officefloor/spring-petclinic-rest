package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The owner's unified member identifier, formatted {@code <REGION><FY><HASH8><CHK>}:
 *
 * <ul>
 * <li>REGION — the postcode-derived region code (e.g. {@code NSW}, or {@code UNKNOWN});
 * <li>FY — the two-digit fiscal year of the business-day-adjusted registration date;
 * <li>HASH8 — the first 8 upper-case hex characters of the SHA-256 of the normalized
 * (E.164) telephone concatenated with the last name (the same hash used by the
 * region-and-hash identity); and
 * <li>CHK — a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}.
 * </ul>
 *
 * <p>The four segments are concatenated with no separators. On collision {@code -<n>} (with
 * the smallest {@code n} of 2 or more) is appended, so the collision suffix is the only part
 * of a member id that contains a {@code '-'}; the REGION and FY can therefore still be
 * recovered from the fixed-width leading base.
 */
public final class MemberId {

    /** Fixed-width tail after REGION: FY(2) + HASH8(8) + CHK(1). */
    private static final int TAIL = 11;

    private MemberId() {
    }

    /**
     * Builds the base member id (before any collision suffix) from its parts.
     *
     * @param region the region code (e.g. {@code NSW})
     * @param fiscalYear the full fiscal year (its last two digits form FY)
     * @param hash8 the 8-character upper-hex HASH8
     * @return {@code <REGION><FY><HASH8><CHK>}
     */
    public static String format(String region, int fiscalYear, String hash8) {
        String base = region + String.format("%02d", Math.floorMod(fiscalYear, 100)) + hash8;
        return base + luhn(base);
    }

    /**
     * @return the REGION segment of {@code memberId}, or {@code null} when it is {@code null}
     *         or too short to contain a region
     */
    public static String region(String memberId) {
        String base = base(memberId);
        return base != null && base.length() > TAIL ? base.substring(0, base.length() - TAIL) : null;
    }

    /**
     * @return the two-digit fiscal-year (FY) segment of {@code memberId}, or {@code null} when
     *         it is {@code null} or too short to contain one
     */
    public static String fiscalYearDigits(String memberId) {
        String base = base(memberId);
        if (base == null || base.length() < TAIL) {
            return null;
        }
        int start = base.length() - TAIL;
        return base.substring(start, start + 2);
    }

    /** The base member id — everything before any {@code -<n>} collision suffix. */
    private static String base(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        return dash >= 0 ? memberId.substring(0, dash) : memberId;
    }

    /** Single Luhn check digit (0-9) over the digits contained in {@code s}; non-digits ignored. */
    static int luhn(String s) {
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
