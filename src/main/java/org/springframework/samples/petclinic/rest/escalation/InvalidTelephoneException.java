package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's telephone does not contain exactly 10 digits after every
 * non-digit character has been stripped. Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters");
        this.rejectedValue = rejectedValue;
    }

    /** The original telephone value that failed normalization. */
    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
