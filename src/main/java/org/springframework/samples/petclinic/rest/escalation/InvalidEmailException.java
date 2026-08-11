package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when an owner request carries an {@code email} that is present but not a
 * syntactically valid address. Carries the offending value so the handler can report
 * what was actually seen.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
