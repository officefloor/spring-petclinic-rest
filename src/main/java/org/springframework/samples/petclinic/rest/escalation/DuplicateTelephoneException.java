package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a normalized telephone that is already
 * used by another owner. Handled as a 409 Conflict.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use by another owner: " + telephone);
    }
}
