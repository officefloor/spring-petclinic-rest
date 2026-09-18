package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries a postcode that is present but either not a 4-digit value or
 * out of range for the city's region (see {@code RegionPostcodes}). Handled by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode, String city) {
        super("Postcode must be a 4-digit value valid for the city's region, but was '" + postcode
                + "' for city: " + city);
    }
}
