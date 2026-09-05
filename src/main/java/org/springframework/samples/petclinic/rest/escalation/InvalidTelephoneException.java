package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that cannot form valid E.164
 * (fewer than 8 or more than 15 digits after the '+'). Handled by
 * {@link InvalidTelephoneHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
