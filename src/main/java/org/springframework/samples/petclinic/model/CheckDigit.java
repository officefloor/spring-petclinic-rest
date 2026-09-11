package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's {@code checkDigit}: a single Luhn check digit (0-9) computed over
 * the digits contained in the owner's {@code customerCode}.
 *
 * <p>The digits are processed right-to-left, doubling every second digit starting with
 * the rightmost (subtracting 9 when a doubled digit exceeds 9); the check digit is the
 * amount needed to bring the running total to the next multiple of ten. Non-digit
 * characters in the customer code are ignored. Null when the owner has no customer code.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit for the given owner, or null when it has no customer code. */
    public static Integer of(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return null;
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
