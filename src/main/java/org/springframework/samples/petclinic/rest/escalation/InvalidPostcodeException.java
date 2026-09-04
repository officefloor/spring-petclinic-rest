package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a postcode that is either not four digits or, for a
 * city with a known region, falls outside that region's inclusive range (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). Handled by {@link InvalidPostcodeExceptionHandler}, which
 * responds 400. Postcode is optional, so this is never thrown when no postcode is supplied.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
