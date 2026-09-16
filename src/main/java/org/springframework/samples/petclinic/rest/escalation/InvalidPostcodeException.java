package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries a {@code postcode} that is present but not a
 * valid 4-digit postcode for the owner's city (either malformed or outside the city's
 * region range). Handled by {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode, String reason) {
        super("Invalid postcode '" + postcode + "': " + reason);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
