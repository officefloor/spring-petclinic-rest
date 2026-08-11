package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's normalized telephone is already used by another owner.
 * Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409 Conflict with the
 * offending value.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone is already used by another owner: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
