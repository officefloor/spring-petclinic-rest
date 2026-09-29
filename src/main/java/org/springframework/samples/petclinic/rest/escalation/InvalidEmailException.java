package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerEmail} when an owner request carries an {@code email} that is present
 * (non-blank) but is not a syntactically valid address. Carries the offending value so
 * {@link InvalidEmailExceptionHandler} can report a 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address when present, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
