package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email whose domain belongs to a disposable-email
 * provider. Handled by {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String domain) {
        super("Email domain " + domain + " is not allowed");
    }
}
