package org.springframework.samples.petclinic.rest.validation;

/**
 * Computes the single Luhn check digit ({@code 0-9}) over the digits contained in a string. Non-digit
 * characters (such as a {@code '<REGION>'} prefix or the hex letters {@code A-F} of an
 * identity code) are ignored, so a check digit can be taken directly over a formatted identifier.
 * Keeping the algorithm in one place lets every derived identifier that carries a Luhn digit compute it
 * identically, differing only in the value it is taken over.
 *
 * <p>Exposed as a static primitive rather than an injected {@code @Component} so it can be reached both
 * from the mapper's {@code default} methods - which, being a MapStruct interface, cannot hold injected
 * collaborators - and from the controller, exactly as the shared {@code Region} primitive is used from
 * both sides.
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * The single Luhn check digit ({@code 0-9}) over the digits contained in {@code value}. Characters
     * that are not decimal digits are skipped, so the digit is taken only over the {@code 0-9} runs of a
     * formatted identifier. The rightmost digit is doubled first (the classic Luhn parity), matching the
     * scheme used to check the derived member id.
     *
     * @param value the string to take the check digit over
     * @return the Luhn check digit, between {@code 0} and {@code 9}
     */
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
