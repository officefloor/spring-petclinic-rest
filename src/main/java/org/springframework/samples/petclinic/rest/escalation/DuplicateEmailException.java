package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's lower-cased email is already used by another owner. Carries the
 * offending value so {@link DuplicateEmailExceptionHandler} can report it. Handled as a 409, distinct
 * from {@link InvalidEmailException}'s 400 for a malformed address.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email is already in use: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
