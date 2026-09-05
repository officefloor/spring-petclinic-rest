package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an {@code email} whose domain is on the
 * disposable-domain blocklist (e.g. {@code mailinator.com}). Handled by
 * {@link DisposableEmailDomainHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String message) {
        super(message);
    }
}
