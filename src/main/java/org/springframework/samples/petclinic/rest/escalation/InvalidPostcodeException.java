package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a postcode that is not four digits, or that is out
 * of range for the owner's city region &mdash; NSW (Sydney) accepts 2000-2099, VIC (Melbourne)
 * 3000-3099 and QLD (Brisbane) 4000-4099; a city with no known region accepts any 4-digit
 * postcode. Validated only WHEN PRESENT (an absent postcode is accepted). Handled by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400 with the offending value.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
