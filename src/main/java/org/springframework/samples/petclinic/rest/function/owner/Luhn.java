package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The standard Luhn check digit (0-9), computed over the decimal digits contained in a string;
 * non-digit characters (such as hyphen separators) are ignored. The single home for the Luhn
 * algorithm the owner functions use, so everything that needs a check digit computes it the same
 * way — {@link MemberId} derives the member id's CHK segment through here.
 */
final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit over the decimal digits contained in {@code digitsSource}. */
    static int of(String digitsSource) {
        int sum = 0;
        boolean dbl = true;
        for (int i = digitsSource.length() - 1; i >= 0; i--) {
            char c = digitsSource.charAt(i);
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
