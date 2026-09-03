package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an {@code email} whose domain is on the disposable-domain
 * blocklist (e.g. {@code mailinator.com}). Handled globally by {@link DisposableEmailExceptionHandler},
 * which responds 400.
 */
public class DisposableEmailException extends Exception {

    private final String email;

    public DisposableEmailException(String email) {
        super("Email domain is on the disposable-domain blocklist: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
