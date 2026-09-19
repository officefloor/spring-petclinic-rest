package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email address whose domain is on the
 * disposable-domain blocklist. Handled by {@link DisposableEmailDomainExceptionHandler},
 * which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email address uses a disposable domain: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
