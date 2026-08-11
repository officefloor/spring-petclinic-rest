package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code checkDigit}: the single Luhn check digit (0..9) computed over
 * the digits contained in {@code customerCode}. Non-digit characters (the '-' separators)
 * are ignored. Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper for
 * an implicit mapping method.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /** The Luhn check digit (0..9) over the digits of {@code owner}'s customerCode. */
    public static int of(Owner owner) {
        return luhn(owner.getCustomerCode());
    }

    /** Luhn check digit (0..9) over the digits contained in {@code value}. */
    private static int luhn(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
