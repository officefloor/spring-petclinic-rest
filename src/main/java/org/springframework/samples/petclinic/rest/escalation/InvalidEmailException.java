package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an {@code email} that is present but not a
 * syntactically valid address. Handled by {@link InvalidEmailExceptionHandler},
 * which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
