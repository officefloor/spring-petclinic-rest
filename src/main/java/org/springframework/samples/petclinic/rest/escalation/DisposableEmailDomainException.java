package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an email whose domain is on the
 * disposable-domain blocklist. Handled by {@link DisposableEmailDomainExceptionHandler},
 * which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String email) {
        super("Disposable email domains are not accepted: " + email);
    }
}
