package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a normalized telephone that is already used by another
 * owner. Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone is already used by another owner: " + telephone);
    }
}
