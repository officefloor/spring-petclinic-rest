package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidatePostcode} when a create request supplies a postcode that is not 4 digits
 * or is out of range for the city's region. Handled by {@link InvalidPostcodeExceptionHandler},
 * which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Invalid postcode: " + postcode);
    }
}
