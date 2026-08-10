package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone is not exactly 10 digits after every non-digit
 * character has been stripped. Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
