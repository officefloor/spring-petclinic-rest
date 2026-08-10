package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request supplies an {@code email} whose domain is on the
 * disposable-domain blocklist (e.g. {@code mailinator.com}). The address is syntactically valid but
 * not accepted. Handled globally by {@link OwnerEmailDisposableExceptionHandler}, which responds 400.
 */
public class OwnerEmailDisposableException extends Exception {

    public OwnerEmailDisposableException(String email) {
        super("Email domain is not accepted (disposable-domain blocklist): " + email);
    }
}
