package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries an email address whose domain is on the
 * disposable-domain blocklist (mailinator.com, tempmail.com, guerrillamail.com). Checked so it
 * appears in a {@code throws} clause and can be routed to its escalation handler, which responds
 * 400 Bad Request.
 */
public class DisposableEmailDomainException extends Exception {

    private final String domain;

    public DisposableEmailDomainException(String domain) {
        super("Email domain is not allowed: " + domain);
        this.domain = domain;
    }

    /** The disposable domain that caused the rejection. */
    public String getDomain() {
        return this.domain;
    }
}
