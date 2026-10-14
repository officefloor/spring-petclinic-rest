package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's email address is not acceptable, e.g. at a disposable email domain.
 */
public class InvalidEmailException extends RuntimeException {

    private final String rejectedValue;

    public InvalidEmailException(String rejectedValue, String message) {
        super(message);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }
}
