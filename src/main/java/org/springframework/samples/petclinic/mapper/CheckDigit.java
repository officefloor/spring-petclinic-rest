package org.springframework.samples.petclinic.mapper;

/**
 * Computes the Luhn check digit over the digits contained in a string.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for a mapping method and apply it to unrelated fields.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code s}, or {@code null} when
     * {@code s} is {@code null}. Non-digit characters are ignored.
     */
    public static Integer luhn(String s) {
        if (s == null) {
            return null;
        }
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
