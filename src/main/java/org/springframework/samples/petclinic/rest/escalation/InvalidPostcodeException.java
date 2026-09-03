package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureOwnerPostcodeWithinRegion} when a create request supplies a
 * postcode that is out of range for the city's region. Handled by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
