package org.springframework.samples.petclinic.util;

/**
 * Derives values from an owner's {@code customerCode}. The {@code checkDigit} is a single
 * Luhn check digit computed over the decimal digits contained in the code (non-digit
 * characters such as the '-' separators are ignored), doubling every second digit from
 * the right. This is the standard Luhn algorithm restated over the code's digits.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The Luhn check digit (0-9) over the decimal digits contained in {@code code}. */
    public static int checkDigit(String code) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
