package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code ValidateOwnerEmailDomain} when an owner email is present and its
 * domain is on the disposable-domain blocklist. Handled by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String email) {
        super("Email domain is not permitted (disposable address): " + email);
    }
}
