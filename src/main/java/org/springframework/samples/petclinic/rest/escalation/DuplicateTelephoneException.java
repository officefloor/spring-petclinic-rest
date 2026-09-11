package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies a telephone whose normalized form is already
 * used by an existing owner. Handled by {@link DuplicateTelephoneExceptionHandler},
 * which responds 409 Conflict.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("An owner with telephone '" + telephone + "' already exists");
    }
}
