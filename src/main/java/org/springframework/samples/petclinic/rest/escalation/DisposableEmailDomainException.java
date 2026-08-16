package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner}
 * when the owner email is syntactically valid but its domain is on the disposable-domain
 * blocklist (e.g. {@code mailinator.com}).
 * Handled globally by {@link DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is not permitted (disposable address): " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
