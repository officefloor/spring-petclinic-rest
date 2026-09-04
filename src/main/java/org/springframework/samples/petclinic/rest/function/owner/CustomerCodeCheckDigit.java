package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The Luhn check digit (0-9) computed over the decimal digits contained in an owner's
 * {@code customerCode}. Non-digit characters (letters, separators) are ignored, so the digit is
 * derived purely from the numeric portion of the code (e.g. the {@code 0007} sequence in
 * {@code LON-SMI-0007}).
 */
public final class CustomerCodeCheckDigit {

    private CustomerCodeCheckDigit() {
    }

    /** The check digit of an already-assigned owner's customer code. */
    public static Integer of(Owner owner) {
        String customerCode = owner.getCustomerCode();
        return customerCode == null ? null : luhn(customerCode);
    }

    /** Luhn check digit (0-9) over the decimal digits contained in {@code value}. */
    static int luhn(String value) {
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
