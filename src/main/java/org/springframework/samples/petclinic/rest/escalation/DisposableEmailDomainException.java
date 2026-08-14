package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectDisposableEmailDomain} when a create-owner request carries an email whose
 * domain is on the disposable-domain blocklist (e.g. mailinator.com). Handled by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is not allowed (disposable address): " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
