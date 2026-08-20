package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating or updating an owner whose supplied email address uses a domain on the
 * disposable-domain blocklist (e.g. {@code mailinator.com}). Handled by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String domain) {
        super("Email domain is not allowed: " + domain);
    }
}
