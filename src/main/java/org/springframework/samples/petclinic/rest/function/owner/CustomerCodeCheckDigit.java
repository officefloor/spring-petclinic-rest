package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The derived {@code checkDigit} for an owner: a single Luhn check digit (0-9) computed over the
 * digits contained in the owner's {@code customerCode}.
 *
 * <p>Non-digit characters (the dashes in {@code <CITY3>-<LAST3>-<NNNN>}) are ignored. The digits are
 * processed right-to-left, doubling every second digit starting with the rightmost and subtracting 9
 * from any doubled value above 9; the check digit is {@code (10 - (sum % 10)) % 10}.
 */
public final class CustomerCodeCheckDigit {

    private CustomerCodeCheckDigit() {
    }

    /** The Luhn check digit over the digits contained in {@code customerCode} (0 when null). */
    public static int of(String customerCode) {
        if (customerCode == null) {
            return 0;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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
