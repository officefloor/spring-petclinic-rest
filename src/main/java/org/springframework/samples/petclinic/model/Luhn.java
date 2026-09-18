package org.springframework.samples.petclinic.model;

/**
 * Computes the Luhn check digit over the decimal digits contained in a string,
 * ignoring any non-digit characters. Used to derive an owner's check digit from its
 * customer code.
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code value}; non-digit
     * characters are ignored and a {@code null} value is treated as containing no digits.
     */
    public static int checkDigit(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean doubling = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubling) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }
}
