package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a postcode outside the range valid for the
 * city's region. Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode, String city) {
        super("Postcode " + postcode + " is not valid for city " + city);
    }
}
