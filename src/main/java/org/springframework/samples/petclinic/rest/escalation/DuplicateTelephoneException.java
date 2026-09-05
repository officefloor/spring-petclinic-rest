package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequireUniqueTelephone}
 * when a create-owner request's normalized telephone is already used by another owner.
 * Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
    }
}
