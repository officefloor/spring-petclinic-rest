package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's check digit: a single Luhn check digit computed over the
 * digits contained in the owner's customer code.
 *
 * <p>Non-digit characters (such as the customer code's separators) are ignored,
 * so only the numeric characters contribute to the checksum. The result is the
 * standard Luhn check digit in the range {@code 0-9}.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic mapping method and apply it to
 * unrelated fields; the mapper references it only through an explicit expression.
 */
public final class CheckDigitDeriver {

    private CheckDigitDeriver() {
    }

    /**
     * Returns the Luhn check digit (0-9) over the digits contained in {@code s},
     * treating a {@code null} value as having no digits (check digit {@code 0}).
     */
    public static int checkDigit(String s) {
        int sum = 0;
        boolean dbl = true;
        if (s != null) {
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
        }
        return (10 - (sum % 10)) % 10;
    }
}
