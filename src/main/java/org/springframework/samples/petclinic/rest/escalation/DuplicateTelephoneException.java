package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a normalized telephone number that is already used by
 * another owner. Handled globally by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
