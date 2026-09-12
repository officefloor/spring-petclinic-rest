package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's {@code checkDigit}: a single {@link Luhn} check digit (0-9) computed
 * over the digits contained in the owner's {@code customerCode}. Non-digit characters in
 * the customer code are ignored. Null when the owner has no customer code.
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
        return Luhn.digit(customerCode);
    }
}
