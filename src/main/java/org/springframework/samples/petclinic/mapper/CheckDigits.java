package org.springframework.samples.petclinic.mapper;

/**
 * Computes the Luhn check digit over the digits contained in a string. Non-digit
 * characters are ignored, so it applies directly to the '<REGION><FY><HASH8>' prefix
 * of a memberId. Kept as a plain static helper (not a mapper method) so MapStruct does
 * not treat it as an implicit mapping method.
 */
public final class CheckDigits {

    private CheckDigits() {
    }

    /**
     * The Luhn check digit (0-9) over the digits contained in {@code s}. Returns null
     * when {@code s} is null.
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
