package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's postcode is not valid for the region of the owner's city.
 */
public class InvalidPostcodeException extends RuntimeException {

    private final String rejectedValue;

    public InvalidPostcodeException(String rejectedValue, String message) {
        super(message);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }
}
