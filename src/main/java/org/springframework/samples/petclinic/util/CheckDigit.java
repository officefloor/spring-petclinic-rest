package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit over the decimal digits contained in a value, ignoring any
 * non-digit characters. A pure function of the value; used for the CHK segment of the
 * {@link MemberId}.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * The Luhn check digit (0-9) over the digits in {@code value}, or {@code null} when
     * {@code value} is {@code null}.
     */
    public static Integer of(String value) {
        if (value == null) {
            return null;
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
