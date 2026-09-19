package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an email that is present but not a syntactically
 * valid address. Handled by {@link InvalidEmailExceptionHandler}, which responds 400 with
 * the rejected value.
 */
public class InvalidEmailException extends Exception {

    private final String rejectedValue;

    public InvalidEmailException(String rejectedValue) {
        super("Email is not a valid address: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
