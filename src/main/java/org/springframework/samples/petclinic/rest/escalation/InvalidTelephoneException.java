package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that does not contain
 * exactly ten digits once every non-digit character has been stripped.
 * Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits: " + telephone);
    }
}
