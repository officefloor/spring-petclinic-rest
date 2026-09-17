package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create- or update-owner request supplies a postcode that is not a 4-digit
 * code, or is out of range for the city's region (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). Handled by {@link InvalidPostcodeExceptionHandler} as a 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException() {
        super("Postcode must be 4 digits and valid for the city's region");
    }
}
