package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create or update an owner carries an email that is present but not a
 * syntactically valid address. Carries the offending value so {@link InvalidEmailExceptionHandler}
 * can report what was actually seen. Checked so it appears in the function's {@code throws} clause
 * and is routed as an escalation.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
