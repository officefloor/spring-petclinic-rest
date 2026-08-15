package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone is not exactly 10 digits after every
 * non-digit character has been stripped. Handled globally by {@link
 * InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + telephone);
    }
}
