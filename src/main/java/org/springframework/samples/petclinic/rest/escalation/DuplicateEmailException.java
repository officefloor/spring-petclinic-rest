package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an email that, lower-cased, is already
 * used by an existing owner. Handled by {@link DuplicateEmailExceptionHandler}, which
 * responds 409.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
