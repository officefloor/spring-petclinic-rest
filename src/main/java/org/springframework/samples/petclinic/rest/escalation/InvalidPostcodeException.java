package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies a postcode that is not four digits, or that is
 * outside the range of the city's region. Handled by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode, String reason) {
        super("Postcode '" + postcode + "' is not valid: " + reason);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
