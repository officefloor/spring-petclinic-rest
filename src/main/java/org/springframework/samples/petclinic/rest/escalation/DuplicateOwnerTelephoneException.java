package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's normalized telephone is already
 * used by another owner. Carries the offending telephone so
 * {@link DuplicateOwnerTelephoneExceptionHandler} can report it. Handled as 409 Conflict.
 */
public class DuplicateOwnerTelephoneException extends Exception {

    private final String telephone;

    public DuplicateOwnerTelephoneException(String telephone) {
        super("Owner telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
