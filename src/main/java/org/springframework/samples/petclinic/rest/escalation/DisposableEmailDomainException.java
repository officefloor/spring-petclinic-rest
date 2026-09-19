package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an email whose domain is on the disposable-domain
 * blocklist. Handled by {@link DisposableEmailDomainExceptionHandler}, which responds 400
 * with the rejected value.
 */
public class DisposableEmailDomainException extends Exception {

    private final String rejectedValue;

    public DisposableEmailDomainException(String rejectedValue) {
        super("Email domain is not allowed: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
