package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose normalized telephone is already used by another owner.
 * Handled globally by {@link DuplicateTelephoneExceptionHandler}, which responds 409 (Conflict).
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone '" + telephone + "' is already used by another owner");
    }
}
