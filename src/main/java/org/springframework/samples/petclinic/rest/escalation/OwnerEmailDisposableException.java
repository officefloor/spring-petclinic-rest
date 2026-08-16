package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the owner supplies an {@code email} whose domain
 * is on the disposable-domain blocklist (mailinator.com, tempmail.com, guerrillamail.com).
 *
 * <p>Carries the offending email so {@link OwnerEmailDisposableExceptionHandler} can respond
 * 400 explaining why the email was rejected.
 */
public class OwnerEmailDisposableException extends Exception {

    private final String email;

    public OwnerEmailDisposableException(String email) {
        super("Email domain is on the disposable-domain blocklist, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
