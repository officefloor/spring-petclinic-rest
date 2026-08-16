package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when a supplied {@code postcode} is malformed (not four
 * digits) or out of range for the owner's city region.
 *
 * <p>Carries the offending postcode so {@link OwnerPostcodeInvalidExceptionHandler} can respond
 * 400.
 */
public class OwnerPostcodeInvalidException extends Exception {

    private final String postcode;

    public OwnerPostcodeInvalidException(String postcode) {
        super("Invalid postcode: " + postcode);
        this.postcode = postcode;
    }

    public String getPostcode() {
        return postcode;
    }
}
