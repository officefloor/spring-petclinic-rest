package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectDisposableEmailDomain} when an owner request supplies an {@code email}
 * whose domain is on the disposable-domain blocklist. Handled globally by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is not accepted (disposable-domain blocklist): " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
