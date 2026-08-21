package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies a normalized telephone already used by another
 * owner. Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
    }
}
