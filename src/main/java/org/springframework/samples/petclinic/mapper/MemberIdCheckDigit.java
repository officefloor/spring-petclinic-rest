package org.springframework.samples.petclinic.mapper;

/**
 * Computes the Luhn check digit (the {@code CHK} segment) over the digits of a member id body.
 *
 * <p>The member id is formatted {@code '<REGION><FY><HASH8><CHK>'}; {@code CHK} is a single Luhn
 * check digit computed over the digit characters of the {@code '<REGION><FY><HASH8>'} body (e.g.
 * {@code 'NSW271A2B3C4D'}). The region letters contribute no digits; only the digit characters take
 * part in the Luhn calculation, processed right-to-left with every second digit doubled and any
 * result above nine reduced by nine. The check digit is the amount that must be added to the running
 * sum to reach the next multiple of ten.
 *
 * <p>Kept as a standalone helper: a single-argument method declared on a MapStruct mapper would be
 * picked up as an implicit conversion and applied to matching property mappings.
 */
public final class MemberIdCheckDigit {

    private MemberIdCheckDigit() {
    }

    /**
     * Returns the Luhn check digit (0-9) over the digit characters of {@code body}, or {@code 0} when
     * the value is {@code null} or contains no digits.
     */
    public static int of(String body) {
        if (body == null) {
            return 0;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = body.length() - 1; i >= 0; i--) {
            char c = body.charAt(i);
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
