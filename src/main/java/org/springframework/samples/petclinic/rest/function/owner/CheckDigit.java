package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Luhn check digit for an owner's {@code customerCode}. A single digit (0-9) computed over the
 * decimal digits contained in the customer code (non-digit characters such as the hyphen separators
 * are ignored), using the standard Luhn algorithm. Derived purely from the owner's own
 * {@code customerCode}, so it carries no stored state and is seed-independent. Used by the owner
 * mapper to expose {@code checkDigit} on responses.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * The Luhn check digit over the digits of the given customer code, or {@code null} when the
     * customer code is absent (e.g. legacy owners with no customer code).
     */
    public static Integer of(String customerCode) {
        return customerCode == null ? null : Luhn.of(customerCode);
    }
}
