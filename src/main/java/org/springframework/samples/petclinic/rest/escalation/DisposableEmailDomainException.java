package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request includes an {@code email} whose domain is on the disposable-domain
 * blocklist (e.g. mailinator.com, tempmail.com, guerrillamail.com). Handled by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String domain) {
        super("Email domain is not allowed: " + domain);
    }
}
