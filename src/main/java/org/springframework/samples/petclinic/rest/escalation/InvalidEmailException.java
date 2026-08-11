package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an {@code email} that is not a syntactically valid address.
 * Handled by {@link InvalidEmailExceptionHandler}, which responds 400 with the offending value.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email is not a syntactically valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
