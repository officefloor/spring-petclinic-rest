package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The Luhn check-digit algorithm over the digits contained in a string. Non-digit characters are
 * ignored, so it applies unchanged to a mixed code (letters and digits) as well as a bare number.
 *
 * <p>Implemented in-house (rather than pulling in a codec dependency) so the algorithm is fixed and
 * self-contained, mirroring {@link Soundex}. It is the single definition every code that carries a
 * trailing check digit hangs off — the {@code memberId}'s CHK segment today.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) computed over the digits contained in {@code value}. */
    public static int checkDigit(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
