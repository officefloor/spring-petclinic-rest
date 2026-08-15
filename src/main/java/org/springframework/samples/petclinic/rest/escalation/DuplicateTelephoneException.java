package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries a normalized telephone that is already used by an
 * existing owner. Checked so it appears in a {@code throws} clause and can be routed to its
 * escalation handler, which responds 409 Conflict.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    /** The normalized telephone that is already in use. */
    public String getTelephone() {
        return this.telephone;
    }
}
