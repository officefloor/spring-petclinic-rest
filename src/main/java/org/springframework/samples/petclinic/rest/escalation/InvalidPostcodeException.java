package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a supplied 4-digit postcode is out of range for the owner's city region
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099; a city with no known region accepts any
 * 4-digit postcode). Postcode is validated only when present. Handled globally by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode, String city, String region) {
        super("Postcode " + postcode + " is not valid for city '" + city + "' (region " + region + ")");
    }
}
