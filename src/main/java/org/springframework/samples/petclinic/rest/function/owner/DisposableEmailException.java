package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerEmail} when an owner create request carries an {@code email}
 * that is syntactically valid but whose domain is on the disposable-email blocklist (see
 * {@link OwnerEmail#isDisposable(String)}). Checked so it appears in the {@code throws} clause
 * and is routed to its escalation handler, which responds 400.
 */
public class DisposableEmailException extends Exception {

    public DisposableEmailException(String email) {
        super("Email domain is not accepted: " + email);
    }
}
