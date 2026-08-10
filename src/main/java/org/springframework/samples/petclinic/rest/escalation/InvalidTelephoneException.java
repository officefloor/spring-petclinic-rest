package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone cannot be converted into valid E.164 form (8 to 15
 * digits after the '+'). Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone must form a valid E.164 number (8 to 15 digits after the '+'): " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
