package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has a telephone that is not exactly ten digits after every
 * non-digit character is stripped. Handled by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits: " + telephone);
    }
}
