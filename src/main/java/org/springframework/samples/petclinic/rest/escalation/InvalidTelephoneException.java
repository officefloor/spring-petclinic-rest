package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has a telephone that cannot be formed into valid E.164
 * (8 to 15 digits after the '+'). Handled by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must form a valid E.164 number (8 to 15 digits): " + telephone);
    }
}
