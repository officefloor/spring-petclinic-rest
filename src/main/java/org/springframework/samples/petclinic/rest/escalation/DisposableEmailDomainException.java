package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an {@code email} whose domain is on the disposable-domain
 * blocklist (e.g. mailinator.com). Handled by {@link DisposableEmailDomainExceptionHandler}, which
 * responds 400 with the offending value.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is on the disposable-domain blocklist: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
