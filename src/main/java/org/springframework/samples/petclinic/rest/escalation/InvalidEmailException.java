package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerEmail} when a create-owner request carries an email that is
 * present but is not a syntactically valid address. Handled by
 * {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
