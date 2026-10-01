package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's derived {@code checkDigit} — a single Luhn check digit
 * (0-9) computed over the digits contained in the owner's {@code customerCode}. Non-digit
 * characters in the customer code (the separating hyphens) are skipped; the standard Luhn
 * algorithm doubles every second digit from the right, subtracting 9 when a doubled value exceeds
 * 9, and the check digit is the amount needed to round the running sum up to the next multiple of
 * ten.
 *
 * <p>Derived at read time by the owner mapper from the already-stored {@code customerCode}, so it
 * needs no column of its own and can never drift from the code it checks.
 */
public final class OwnerCheckDigit {

    private OwnerCheckDigit() {
    }

    /** The Luhn check digit of an already-stored owner, read from its {@code customerCode}. */
    public static Integer checkDigit(Owner owner) {
        return luhn(owner.getCustomerCode());
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code s}; {@code null} when absent. */
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
