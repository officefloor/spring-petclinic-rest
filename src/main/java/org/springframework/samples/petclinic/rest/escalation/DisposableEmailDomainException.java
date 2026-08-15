package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an {@code email} whose domain is on the disposable-domain
 * blocklist (mailinator.com, tempmail.com, guerrillamail.com). Handled globally by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String email) {
        super("Email domain is not accepted (disposable address): " + email);
    }
}
