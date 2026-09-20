package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerEmail} when a request supplies an email that is not a
 * syntactically valid address. Carries the rejected (raw) value for the handler's message.
 * Checked so it appears in the function's {@code throws} clause and routes to an escalation.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address, but was: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
