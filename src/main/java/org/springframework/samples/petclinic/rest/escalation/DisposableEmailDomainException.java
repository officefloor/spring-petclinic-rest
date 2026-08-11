package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when an owner request carries an {@code email} whose domain is on the
 * disposable-domain blocklist (mailinator.com, tempmail.com, guerrillamail.com). The
 * address is syntactically valid, so this is a policy rejection distinct from
 * {@link InvalidEmailException}. Carries the offending value so the handler can report
 * what was seen.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is not accepted (disposable address): '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
