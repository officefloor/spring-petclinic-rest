package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes an owner's {@code checkDigit} — a single Luhn check digit (0-9) over the digits
 * contained in the owner's {@code customerCode}. Non-digit characters (e.g. the {@code '-'}
 * separators and letter prefixes) are ignored.
 *
 * <p>Standard Luhn: working right-to-left, every second digit starting from the rightmost is
 * doubled (subtracting 9 when the result exceeds 9); the check digit is the amount needed to
 * bring the running total to the next multiple of ten.
 */
public final class CustomerCodeCheckDigit {

    private CustomerCodeCheckDigit() {
    }

    /** The Luhn check digit (0-9) over the digits in {@code customerCode}. */
    public static int of(String customerCode) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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
