package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request includes a {@code postcode} that is not four digits, or
 * that is out of range for the owner's city region. Handled globally by
 * {@link InvalidOwnerPostcodeExceptionHandler}, which responds 400.
 */
public class InvalidOwnerPostcodeException extends Exception {

    private final String postcode;

    public InvalidOwnerPostcodeException(String postcode) {
        super("Invalid postcode: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return this.postcode;
    }
}
