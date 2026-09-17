package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of the <em>Luhn check digit</em>: the digit (0-9) that, appended to
 * a sequence of digits, makes the whole pass the Luhn checksum. Non-digit characters in the
 * input are ignored, so it can be computed directly over a formatted value such as a
 * {@link CustomerCode}.
 *
 * <p>Pure function of its input; the digit is computed on read rather than stored.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code value}. */
    public static int checkDigit(String value) {
        int sum = 0;
        boolean doubled = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubled) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubled = !doubled;
        }
        return (10 - (sum % 10)) % 10;
    }
}
