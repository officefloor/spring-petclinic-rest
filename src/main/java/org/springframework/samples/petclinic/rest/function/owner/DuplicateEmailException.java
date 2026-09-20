package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueEmail} when a create request's normalized (lower-cased) email
 * is already used by an existing owner. Carries the normalized value for the handler's
 * message. Checked so it appears in the function's {@code throws} clause and routes to an
 * escalation.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
