package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that is not exactly ten digits
 * once every non-digit character has been stripped. Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400 with the rejected value.
 */
public class InvalidTelephoneException extends Exception {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
