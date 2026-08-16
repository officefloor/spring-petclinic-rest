package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner}
 * when a supplied owner postcode is not four digits, or is outside the valid range for the
 * owner's city region. Handled globally by {@link InvalidPostcodeExceptionHandler}, which
 * responds 400.
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
