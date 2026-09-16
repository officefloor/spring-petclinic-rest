package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-/update-owner request supplies a postcode that is not four digits or
 * that falls outside the valid range for the city's region. Handled by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
