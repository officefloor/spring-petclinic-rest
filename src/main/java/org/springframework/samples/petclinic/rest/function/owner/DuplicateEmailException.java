package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerEmailUnique} when an owner create request's normalised (lower-cased)
 * email is already in use by an existing owner. Checked so it appears in the {@code throws} clause
 * and is routed to its escalation handler, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
