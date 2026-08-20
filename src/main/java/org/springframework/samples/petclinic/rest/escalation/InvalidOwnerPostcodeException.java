package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerPostcode} when a create/update owner request supplies a
 * postcode that is either malformed (not exactly 4 digits) or out of the valid range for
 * the owner's city region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no
 * known region accepts any 4-digit postcode. Handled by
 * {@link InvalidOwnerPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidOwnerPostcodeException extends Exception {

    public InvalidOwnerPostcodeException(String postcode, String city) {
        super("Invalid postcode '" + postcode + "' for city: " + city);
    }
}
