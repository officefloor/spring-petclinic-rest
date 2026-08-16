package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Assembles and inspects an owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>}:
 * the region code (see {@link CityRegion}), the 2-digit fiscal year FY of the registrationDate
 * (see {@link FiscalYear}), the 8 upper-case hex HASH8 of SHA-256 over {@code normalizedTelephone +
 * lastName} (see {@link OwnerIdentity#memberIdHash}) and a single Luhn check digit CHK computed over
 * the digits of {@code <REGION><FY><HASH8>}. For example {@code NSW261A2B3C4D7}.
 *
 * <p>The base id depends only on this owner's own fields and is stable regardless of how many owners
 * exist; collision handling (see {@link AssignMemberId}) de-duplicates it by appending {@code -<n>}.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * The base {@code memberId} (before any collision suffix) from its {@code region}, 2-digit fiscal
     * year {@code fiscalYearYY} and {@code hash8}: {@code <REGION><FY><HASH8>} with the Luhn CHK of its
     * digits appended.
     */
    public static String of(String region, int fiscalYearYY, String hash8) {
        String body = String.format("%s%02d%s", region, fiscalYearYY, hash8);
        return body + luhn(body);
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code value}; non-digit characters are
     * ignored. Working right-to-left, every second digit starting from the rightmost is doubled
     * (subtracting 9 when the result exceeds 9); the check digit brings the running total to the next
     * multiple of ten.
     */
    public static int luhn(String value) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
