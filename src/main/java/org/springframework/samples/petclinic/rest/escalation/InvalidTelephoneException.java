package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that does not contain
 * exactly 10 digits once every non-digit character has been stripped.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters: " + telephone);
    }
}
