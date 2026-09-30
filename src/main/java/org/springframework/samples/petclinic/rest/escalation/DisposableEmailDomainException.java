package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's supplied email address uses a domain on the disposable-domain
 * blocklist (mailinator.com, tempmail.com, guerrillamail.com). The email is validated only
 * when present; an owner with no email passes through unchanged. Handled globally by
 * {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String domain;

    private final String rejectedValue;

    public DisposableEmailDomainException(String rejectedValue, String domain) {
        super("Email domain " + domain + " is not allowed");
        this.rejectedValue = rejectedValue;
        this.domain = domain;
    }

    /** The disposable domain that was rejected. */
    public String getDomain() {
        return this.domain;
    }

    /** The email value that was rejected. */
    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
