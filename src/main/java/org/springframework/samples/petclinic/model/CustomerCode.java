package org.springframework.samples.petclinic.model;

/**
 * Derives values from an owner's {@code customerCode}.
 *
 * <p>The {@link #checkDigit(String)} rule computes the standard Luhn check digit over the digits
 * contained in the code (non-digit characters, such as the formatting dashes, are ignored). It is the
 * single source of truth for the {@code checkDigit} response field exposed by {@code OwnerMapper}.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /**
     * Compute the Luhn check digit over the digits contained in {@code code}.
     *
     * @param code the customer code; non-digit characters are ignored
     * @return the Luhn check digit, between 0 and 9 inclusive
     */
    public static int checkDigit(String code) {
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
