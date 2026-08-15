package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request supplies a postcode that is not four digits, or that
 * is four digits but falls outside the valid range for the owner's city region (Sydney->NSW
 * 2000-2099, Melbourne->VIC 3000-3099, Brisbane->QLD 4000-4099). Handled globally by
 * {@link InvalidPostcodeExceptionHandler}, which responds 400 naming 'postcode'.
 */
public class InvalidPostcodeException extends Exception {

    private final String postcode;

    public InvalidPostcodeException(String postcode) {
        super("Invalid postcode for owner's city: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return postcode;
    }
}
