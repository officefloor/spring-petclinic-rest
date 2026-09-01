package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureOwnerPostcode} when a supplied postcode is malformed or out of range
 * for the owner's city region. Handled globally by {@link InvalidPostcodeExceptionHandler},
 * which responds 400.
 */
public class InvalidPostcodeException extends Exception {

    public InvalidPostcodeException(String message) {
        super(message);
    }
}
