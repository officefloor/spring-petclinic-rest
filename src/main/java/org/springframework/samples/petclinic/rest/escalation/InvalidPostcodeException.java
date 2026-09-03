package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries a {@code postcode} that is present but not valid: either not a
 * 4-digit value, or out of the range fixed for the city's region (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). A city with no known region accepts any 4-digit postcode. Handled globally by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
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
