package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's lower-cased email is already used by another owner. Handled
 * by {@link DuplicateEmailExceptionHandler}, which responds 409 Conflict with the offending value.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email is already used by another owner: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
