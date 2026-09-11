package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code checkDigit}: a single Luhn check digit (0-9) computed over
 * the digits contained in the owner's {@code customerCode}. Non-digit characters are
 * ignored; the rightmost digit is doubled and doubling alternates leftward.
 */
public final class CheckDigits {

    private CheckDigits() {
    }

    /** The Luhn check digit (0-9) over the digits of the owner's customerCode. */
    public static int forOwner(Owner owner) {
        return luhn(owner.getCustomerCode());
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code value}. */
    public static int luhn(String value) {
        if (value == null) {
            return 0;
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
