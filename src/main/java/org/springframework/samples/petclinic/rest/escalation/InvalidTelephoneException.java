package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a create-owner request carries a telephone that,
 * after every non-digit character is stripped, is not exactly 10 digits. Carries the offending
 * (normalized) value so {@link InvalidTelephoneExceptionHandler} can report a 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String normalized;

    public InvalidTelephoneException(String normalized) {
        super("Telephone must be exactly 10 digits after removing non-digit characters, but was: '"
                + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return this.normalized;
    }
}
