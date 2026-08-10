package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request includes a {@code postcode} that is present but invalid: either
 * not exactly 4 digits, or 4 digits but outside the valid range for the city's region
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit
 * postcode. Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
    }
}
