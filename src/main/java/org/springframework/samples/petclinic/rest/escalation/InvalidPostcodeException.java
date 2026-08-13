package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a supplied postcode is not a 4-digit code, or is a 4-digit code that falls outside
 * the range valid for the city's canonical region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
 * A city with no known region accepts any 4-digit postcode, and an absent postcode is never
 * rejected. Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return postcode;
    }
}
