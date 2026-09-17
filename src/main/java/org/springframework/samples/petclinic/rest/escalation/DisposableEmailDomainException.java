package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request carries an email whose domain is on the disposable-domain blocklist.
 * Carries the offending domain so {@link DisposableEmailDomainExceptionHandler} can report it. Handled
 * as a 400, distinct from {@link InvalidEmailException}'s syntax rejection.
 */
public class DisposableEmailDomainException extends Exception {

    private final String domain;

    public DisposableEmailDomainException(String domain) {
        super("Email domain is not allowed (disposable address): " + domain);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
