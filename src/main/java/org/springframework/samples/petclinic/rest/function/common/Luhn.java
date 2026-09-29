package org.springframework.samples.petclinic.rest.function.common;

/**
 * Computes a single Luhn check digit over the digits contained in a string. Used to derive the
 * {@code CHK} segment of an owner's {@code memberId} from its {@code <REGION><FY><HASH8>} base. The
 * algorithm is the standard Luhn (right-to-left doubling of alternate digits); non-digit characters
 * are ignored, so the region and hash letters in a memberId base do not affect the result. Pinned so
 * both the Spring and OfficeFloor arms produce identical values.
 */
public final class Luhn {

    private Luhn() {
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code s}; non-digits are ignored. */
    public static int checkDigit(String s) {
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
