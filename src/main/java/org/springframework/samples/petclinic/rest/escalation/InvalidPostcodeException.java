package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a postcode that is not four digits or is
 * outside the range for the city's region. Handled by {@link InvalidPostcodeExceptionHandler},
 * which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Invalid postcode: " + postcode);
    }
}
