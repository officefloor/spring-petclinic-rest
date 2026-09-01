package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a supplied postcode is out of the valid range for the owner's city region.
 * Handled by {@link InvalidPostcodeExceptionHandler} as 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String city, String postcode) {
        super("Postcode " + postcode + " is not valid for city: " + city);
    }
}
