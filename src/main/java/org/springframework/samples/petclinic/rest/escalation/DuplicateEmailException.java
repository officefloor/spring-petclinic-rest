package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner carries an email whose lower-cased form already belongs to
 * another owner. Carries the offending (lower-cased) email so {@link DuplicateEmailExceptionHandler}
 * can report it. Checked so it appears in the function's {@code throws} clause and is routed as an
 * escalation (a 409 Conflict).
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("An owner with email '" + email + "' already exists");
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
