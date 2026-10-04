package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerEmail} when an owner create request carries an {@code email}
 * that is present but not a syntactically valid address. Checked so it appears in the
 * {@code throws} clause and is routed to its escalation handler, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
    }
}
