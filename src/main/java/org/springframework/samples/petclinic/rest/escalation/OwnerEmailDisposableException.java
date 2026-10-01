package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner} when an
 * owner email is syntactically valid but its domain is on the disposable-domain blocklist (e.g.
 * mailinator.com). Handled by {@link OwnerEmailDisposableExceptionHandler}, which responds 400.
 */
public class OwnerEmailDisposableException extends Exception {

    private final String domain;

    public OwnerEmailDisposableException(String domain) {
        super("Email domain is on the disposable-domain blocklist: " + domain);
        this.domain = domain;
    }

    public String getDomain() {
        return this.domain;
    }
}
