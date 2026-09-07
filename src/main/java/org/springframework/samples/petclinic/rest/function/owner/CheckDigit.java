package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The Luhn check digit (0-9) computed over the digits contained in a string — used for the CHK
 * segment of a {@link MemberId member id}, computed over the digits of {@code <REGION><FY><HASH8>}.
 * Non-digit characters are ignored; the standard Luhn algorithm doubles every second digit from
 * the right and reduces the running sum modulo ten.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    public static Integer of(String value) {
        if (value == null) {
            return null;
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
