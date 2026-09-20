package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectDisposableEmail} when a request supplies an email whose domain is a
 * known disposable-email provider. Carries the rejected email for the handler's message.
 * Checked so it appears in the function's {@code throws} clause and routes to an escalation.
 */
public class DisposableEmailDomainException extends Exception {

    private final String email;

    public DisposableEmailDomainException(String email) {
        super("Email domain is a disposable-email provider and is not accepted: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
