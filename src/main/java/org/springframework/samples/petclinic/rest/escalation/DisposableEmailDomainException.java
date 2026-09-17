package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an email whose domain is on the disposable-domain
 * blocklist. Email is optional, so this is raised only when a non-blank value is present.
 * Handled by {@link DisposableEmailDomainExceptionHandler} as a 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException() {
        super("Email domain is not accepted");
    }
}
