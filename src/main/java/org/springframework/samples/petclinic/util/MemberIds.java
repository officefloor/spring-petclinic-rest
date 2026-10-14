package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds and parses member ids '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;&lt;CHK&gt;': the upper-case region code, the
 * two-digit fiscal year (see {@link FiscalYears#twoDigit(LocalDate)}), the 8 upper-case hex HASH8 and a
 * Luhn check digit over the digits of '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;'. A member id may carry a
 * '-&lt;n&gt;' de-duplication suffix.
 */
public final class MemberIds {

    private static final Pattern MEMBER_ID = Pattern.compile("^([A-Z]+)([0-9]{2})([0-9A-F]{8})([0-9])(-[0-9]+)?$");

    private MemberIds() {
    }

    /**
     * Builds the member id '&lt;REGION&gt;&lt;FY&gt;&lt;HASH8&gt;&lt;CHK&gt;'.
     *
     * @param region           the region code, e.g. 'NSW'
     * @param registrationDate the date whose fiscal year forms the FY segment
     * @param hash8            the 8 upper-case hex characters
     * @return the member id, e.g. 'NSW273FA9C27B4'
     */
    public static String of(String region, LocalDate registrationDate, String hash8) {
        String body = region + String.format("%02d", FiscalYears.twoDigit(registrationDate)) + hash8;
        return body + luhn(body);
    }

    /**
     * The REGION segment of a member id, or {@code null} when it is not a member id.
     */
    public static String regionOf(String memberId) {
        Matcher m = match(memberId);
        return m == null ? null : m.group(1);
    }

    /**
     * The two-digit FY segment of a member id, or {@code null} when it is not a member id.
     */
    public static Integer fiscalYearOf(String memberId) {
        Matcher m = match(memberId);
        return m == null ? null : Integer.valueOf(m.group(2));
    }

    /**
     * The Luhn check digit (0-9) over the digits of a value, ignoring non-digit characters.
     */
    public static int luhn(String value) {
        int sum = 0;
        boolean doubled = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubled) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubled = !doubled;
        }
        return (10 - sum % 10) % 10;
    }

    private static Matcher match(String memberId) {
        if (memberId == null) {
            return null;
        }
        Matcher m = MEMBER_ID.matcher(memberId);
        return m.matches() ? m : null;
    }
}
