package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose normalized telephone is already used by another owner.
 * Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("An owner with telephone " + telephone + " already exists");
    }
}
