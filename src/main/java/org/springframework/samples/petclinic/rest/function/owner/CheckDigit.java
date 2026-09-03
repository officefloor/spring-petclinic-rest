package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes the Luhn check digit (0-9) over the digits contained in a customer code,
 * doubling from the rightmost digit. Returns {@code null} when the code is absent, so
 * owners without a customer code carry no check digit.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    public static Integer of(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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
