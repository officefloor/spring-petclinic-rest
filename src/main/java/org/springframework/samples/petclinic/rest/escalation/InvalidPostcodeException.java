package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a {@code postcode} that is either malformed (not
 * exactly 4 digits) or out of the valid range for the owner's city region (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). Handled globally by {@link InvalidPostcodeExceptionHandler},
 * which responds 400. Postcode is validated only when present, so an owner created without a
 * postcode never triggers this.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
    }
}
