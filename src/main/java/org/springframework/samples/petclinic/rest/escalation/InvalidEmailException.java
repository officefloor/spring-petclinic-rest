package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies an 'email' that is present but not a
 * syntactically valid address. Handled globally by {@link InvalidEmailExceptionHandler},
 * which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email is not a syntactically valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
