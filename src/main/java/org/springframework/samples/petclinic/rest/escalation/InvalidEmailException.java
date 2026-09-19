package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email address that is not syntactically valid.
 * Handled by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email address is not syntactically valid: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
