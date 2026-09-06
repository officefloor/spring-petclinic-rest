package org.springframework.samples.petclinic.mapper;

/**
 * Computes a single Luhn check digit (0 to 9) over the digits contained in a
 * string. Kept as a plain static helper - rather than a method on
 * {@link OwnerMapper} - so MapStruct does not mistake it for an implicit
 * property mapping.
 *
 * <p>Non-digit characters are ignored, so the digit is derived only from the
 * numeric characters of the input (for example the digits of an owner's
 * member id).
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * Computes the Luhn check digit over the digits contained in {@code s}.
     * The rightmost digit is doubled, alternating leftwards; each doubled
     * value over 9 has 9 subtracted; the check digit is the amount that brings
     * the running sum up to the next multiple of ten.
     *
     * @param s the string whose digits the check digit is computed over
     * @return the Luhn check digit, between 0 and 9 inclusive
     */
    public static int forDigits(String s) {
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
