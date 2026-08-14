package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueEmail} when a create-owner request carries an email whose
 * lower-cased value is already used by another owner. Handled by
 * {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already used by another owner: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
