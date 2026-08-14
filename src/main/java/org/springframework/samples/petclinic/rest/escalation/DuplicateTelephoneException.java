package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueTelephone} when a create-owner request carries a normalized
 * telephone that is already used by another owner. Handled by
 * {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already used by another owner: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
