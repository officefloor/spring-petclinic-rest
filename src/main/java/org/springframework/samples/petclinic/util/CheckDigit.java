package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit (0-9) over the decimal digits contained in a string, ignoring any
 * non-digit characters. Used to derive an owner's {@code checkDigit} from their {@code customerCode}.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    public static int luhn(String s) {
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
