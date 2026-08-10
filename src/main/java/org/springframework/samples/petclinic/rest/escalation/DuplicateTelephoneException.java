package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's normalized telephone is already used by another owner.
 * Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409 (Conflict).
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("An owner with telephone '" + telephone + "' already exists");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
