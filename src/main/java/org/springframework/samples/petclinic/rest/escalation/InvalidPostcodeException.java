package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating or updating an owner whose supplied postcode is malformed (not four digits)
 * or out of range for the owner's city region. Handled by {@link InvalidPostcodeExceptionHandler},
 * which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String postcode) {
        super("Postcode is not valid for the owner's city: " + postcode);
    }
}
