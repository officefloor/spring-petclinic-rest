package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's telephone cannot be converted to a valid E.164 number (8 to 15
 * digits after the '+', once spaces, dashes and brackets are stripped and the '+61'
 * default country code is applied). Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        this.rejectedValue = rejectedValue;
    }

    /** The original telephone value that failed normalization. */
    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
