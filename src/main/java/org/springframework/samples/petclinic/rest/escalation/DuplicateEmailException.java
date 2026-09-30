package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lower-cased email is already used by another
 * owner. Handled globally by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("An owner with email " + email + " already exists");
        this.email = email;
    }

    /** The lower-cased email that is already in use. */
    public String getEmail() {
        return this.email;
    }
}
