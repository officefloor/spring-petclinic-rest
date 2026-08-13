package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the normalized (lower-cased) email is already used by another owner.
 * Handled by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
