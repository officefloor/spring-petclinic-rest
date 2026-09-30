package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerEmailDomain} when a supplied email's domain is on the
 * disposable-domain blocklist ({@code mailinator.com}, {@code tempmail.com},
 * {@code guerrillamail.com}). Handled globally by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400. Email is optional, so an
 * absent email never reaches this check.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String message) {
        super(message);
    }
}
