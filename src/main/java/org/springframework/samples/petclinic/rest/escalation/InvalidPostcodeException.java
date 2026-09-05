package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request carries a postcode that is not a 4-digit code,
 * or that is out of the valid range for the owner's city region (NSW 2000-2099, VIC
 * 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode.
 * Handled by {@link InvalidPostcodeHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
