package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a supplied owner email uses a disposable-email domain
 * (mailinator.com, tempmail.com, guerrillamail.com). The email is checked only when
 * present; an omitted email is always accepted. Handled globally by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String email, String domain) {
        super("Email " + email + " uses disposable domain '" + domain + "'");
    }
}
