package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create/update when the supplied email is present but not a syntactically valid
 * address. Handled by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
