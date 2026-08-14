package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerPostcode} when a create-owner request carries a postcode that is
 * present but is not four digits, or is out of range for the owner's city's region (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode. Handled
 * by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode) {
        super("Postcode must be four digits valid for the city's region: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
