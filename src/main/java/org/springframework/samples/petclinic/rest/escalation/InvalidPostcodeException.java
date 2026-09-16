package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create- or update-owner request supplies a 4-digit postcode that is out of
 * range for the owner's city region. Handled by {@link InvalidPostcodeExceptionHandler}, which
 * responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String city, String postcode) {
        super("Postcode " + postcode + " is not valid for the region of city: " + city);
    }
}
