package org.springframework.samples.petclinic.util;

/**
 * Computes the Luhn check digit over the digits contained in a string. Non-digit
 * characters are ignored, so the algorithm applies unchanged to formatted values such as
 * an owner's customer code ({@code PAR-FRA-0007}).
 *
 * <p>Walking right to left, every second digit (starting with the rightmost) is doubled,
 * casting out nines, and the check digit is the amount needed to bring the running total
 * to a multiple of ten.
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * @param value the string whose digits are checked (non-digits are ignored)
     * @return the Luhn check digit, {@code 0}-{@code 9}
     */
    public static int of(String value) {
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
