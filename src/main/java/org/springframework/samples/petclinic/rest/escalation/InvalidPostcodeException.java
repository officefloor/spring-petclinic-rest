package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequirePostcode}
 * when a create-owner request supplies a postcode that is not 4 digits or is out of range for
 * the city's region. Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Invalid postcode: " + postcode);
    }
}
