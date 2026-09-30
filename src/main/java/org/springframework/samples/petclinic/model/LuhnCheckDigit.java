package org.springframework.samples.petclinic.model;

/**
 * The single definition of the Luhn check digit: a single digit (0-9) computed over the
 * digits contained in a string, with non-digit characters ignored. Used by the mapping
 * layer to derive an owner's {@code checkDigit} from its {@code customerCode}.
 *
 * <p>Lives beside {@link Owner} — the same home as {@link CityRegions} and
 * {@link OwnerIdentity} — so any layer can depend on it without a package cycle.
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * The Luhn check digit (0-9) over the digits in {@code value}, ignoring any non-digit
     * characters. {@code null} when {@code value} is {@code null}.
     */
    public static Integer of(String value) {
        if (value == null) {
            return null;
        }
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
