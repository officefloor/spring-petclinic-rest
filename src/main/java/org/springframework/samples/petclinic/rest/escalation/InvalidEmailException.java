package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email that is not a syntactically valid address.
 * Email is optional, so a missing or blank email never triggers this; only a present-but-invalid
 * value does. Handled by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String rejectedValue;

    public InvalidEmailException(String rejectedValue) {
        super("Email must be a syntactically valid address: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
