package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's supplied registration date is invalid, e.g. later than the server's current date.
 */
public class InvalidRegistrationDateException extends RuntimeException {

    private final String rejectedValue;

    public InvalidRegistrationDateException(String rejectedValue, String message) {
        super(message);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }
}
