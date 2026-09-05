package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * The unified member identifier, formatted {@code <REGION><FY><HASH8><CHK>}: the region
 * code, the 2-digit fiscal year, the 8 upper-hex HASH8 (see {@link AssignCustomerCode}) and
 * a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}. The three
 * trailing segments are fixed width, so the region is whatever precedes them.
 */
public final class MemberId {

    private static final int HASH8_LEN = 8;
    private static final int FY_LEN = 2;
    private static final int TAIL = FY_LEN + HASH8_LEN + 1; // FY + HASH8 + CHK

    private MemberId() {
    }

    /** Builds the id from its region, the fiscal year of {@code registrationDate} and HASH8. */
    public static String of(String region, LocalDate registrationDate, String hash8) {
        String body = String.format("%s%02d%s", region, FiscalYear.of(registrationDate) % 100, hash8);
        return body + CheckDigit.of(body);
    }

    /** The region segment: everything before the fixed-width tail. */
    public static String region(String memberId) {
        return memberId.substring(0, memberId.length() - TAIL);
    }

    /** The {@code FY<YY>} label carried by the id. */
    public static String fiscalYear(String memberId) {
        int fyEnd = memberId.length() - (HASH8_LEN + 1);
        return "FY" + memberId.substring(fyEnd - FY_LEN, fyEnd);
    }
}
