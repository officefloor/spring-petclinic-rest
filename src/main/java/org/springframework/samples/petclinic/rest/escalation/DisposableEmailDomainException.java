package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create/update when the supplied email's domain is on the disposable-domain blocklist
 * (e.g. mailinator.com). Handled by {@link DisposableEmailDomainExceptionHandler}, which
 * responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is on the disposable-domain blocklist: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
