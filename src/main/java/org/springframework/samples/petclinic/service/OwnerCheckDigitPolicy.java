package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code checkDigit} is a single Luhn check digit (0-9) computed
 * over the digits contained in the owner's {@code customerCode}. Kept as a small,
 * self-contained unit so the rule can be applied from the read flow without adding
 * complexity to the mapper, controller, or service.
 */
public final class OwnerCheckDigitPolicy {

    private OwnerCheckDigitPolicy() {
    }

    /**
     * Derive the Luhn {@code checkDigit} for the given owner, or {@code null} when the
     * customer code it is built from is not yet available.
     *
     * @param owner the owner whose check digit to derive
     * @return the Luhn check digit, or {@code null} if it cannot be derived
     */
    public static Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        return customerCode == null ? null : luhn(customerCode);
    }

    /** Luhn check digit (0-9) over the digits contained in {@code s}. */
    private static int luhn(String s) {
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
