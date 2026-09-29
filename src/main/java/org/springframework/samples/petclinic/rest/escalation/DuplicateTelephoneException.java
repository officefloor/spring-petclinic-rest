package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose normalized telephone is already used by another
 * owner. Handled globally by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("An owner with telephone " + telephone + " already exists");
        this.telephone = telephone;
    }

    /** The normalized telephone that is already in use. */
    public String getTelephone() {
        return this.telephone;
    }
}
