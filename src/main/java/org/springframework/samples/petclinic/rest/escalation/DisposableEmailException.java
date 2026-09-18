package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an email whose domain is on the disposable-domain
 * blocklist. Handled by {@link DisposableEmailExceptionHandler}, which responds 400.
 */
public class DisposableEmailException extends Exception {

    public DisposableEmailException(String domain) {
        super("Email domain is not allowed: " + domain);
    }
}
