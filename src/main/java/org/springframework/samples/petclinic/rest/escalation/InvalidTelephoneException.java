package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that cannot be normalized to a
 * valid E.164 number (a '+' followed by 8 to 15 digits). Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400 with the rejected value.
 */
public class InvalidTelephoneException extends Exception {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone cannot be normalized to a valid E.164 number: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
