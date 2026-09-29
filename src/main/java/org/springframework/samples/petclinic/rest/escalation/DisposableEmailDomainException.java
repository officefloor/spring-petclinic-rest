package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireEmailNotDisposable} when an owner request carries an {@code email} whose domain
 * is on the disposable-domain blocklist. Carries the offending value so
 * {@link DisposableEmailDomainExceptionHandler} can report a 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email must not use a disposable domain, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
