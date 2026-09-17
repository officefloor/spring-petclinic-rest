package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email whose domain is on the disposable-domain
 * blocklist. Handled by {@link DisposableEmailExceptionHandler}, which responds 400.
 */
public class DisposableEmailException extends Exception {

    public DisposableEmailException(String email) {
        super("Email domain is not accepted: " + email);
    }
}
