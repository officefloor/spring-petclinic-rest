package org.springframework.samples.petclinic.util;

/**
 * Computes the single Luhn check digit (0-9) over the decimal digits contained in a value,
 * ignoring any non-digit characters. Used to derive an owner's {@code checkDigit} from its
 * {@code customerCode}.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit over the digits of {@code value}. */
    public static int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl && (d *= 2) > 9) {
                d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
