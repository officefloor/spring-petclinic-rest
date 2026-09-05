package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes the Luhn check digit (0-9) over the decimal digits contained in a string. Non-digit
 * characters (region letters and the hex letters {@code A-F} of the hash) are ignored. Used to
 * produce the {@code CHK} segment of the {@code memberId}, taken over the decimal digits of the
 * {@code <REGION><FY><HASH8>} core (see {@link AssignMemberId}).
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit over the digits of {@code s}, or 0 when {@code s} is null. */
    public static int of(String s) {
        if (s == null) {
            return 0;
        }
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
