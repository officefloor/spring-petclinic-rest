package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes the single Luhn check digit (0-9) over the digits contained in a value. The
 * rightmost digit is doubled, alternating leftwards; each doubled value above nine has
 * nine subtracted, and the digit that makes the running sum a multiple of ten is
 * returned. Used for the {@code CHK} segment of the memberId.
 */
public class CheckDigit {

    public static int checkDigit(String value) {
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
