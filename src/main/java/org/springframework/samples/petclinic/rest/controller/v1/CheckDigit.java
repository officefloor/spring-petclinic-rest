package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single Luhn check digit (0-9) computed over the digits of an owner's customerCode,
 * so a code can be validated without recomputing how it was built.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit over the digits contained in {@code owner}'s customerCode. */
    public static int of(Owner owner) {
        return luhn(owner.getCustomerCode());
    }

    /** The Luhn check digit (0-9) over the digits contained in {@code value}. */
    static int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            int d = value.charAt(i) - '0';
            if (d < 0 || d > 9) {
                continue;
            }
            if (dbl && (d *= 2) > 9) {
                d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
