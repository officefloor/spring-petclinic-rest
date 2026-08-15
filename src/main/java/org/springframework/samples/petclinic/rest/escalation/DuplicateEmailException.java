package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries an email whose lower-cased form is already used by an
 * existing owner. Checked so it appears in a {@code throws} clause and can be routed to its
 * escalation handler, which responds 409 Conflict.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
        this.email = email;
    }

    /** The lower-cased email that is already in use. */
    public String getEmail() {
        return this.email;
    }
}
