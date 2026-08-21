package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request includes an {@code email} that is present but not a
 * syntactically valid address. Handled globally by {@link InvalidOwnerEmailExceptionHandler},
 * which responds 400.
 */
public class InvalidOwnerEmailException extends Exception {

    private final String email;

    public InvalidOwnerEmailException(String email) {
        super("Invalid email address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
