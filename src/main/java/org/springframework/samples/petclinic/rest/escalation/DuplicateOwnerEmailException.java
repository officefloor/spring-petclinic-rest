package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's lower-cased email is already used
 * by another owner. Carries the offending email so {@link DuplicateOwnerEmailExceptionHandler}
 * can report it. Handled as 409 Conflict.
 */
public class DuplicateOwnerEmailException extends Exception {

    private final String email;

    public DuplicateOwnerEmailException(String email) {
        super("Owner email already in use: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
