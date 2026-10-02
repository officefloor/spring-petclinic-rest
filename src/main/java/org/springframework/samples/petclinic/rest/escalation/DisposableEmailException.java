package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create or update an owner carries an email whose domain is on the
 * disposable-domain blocklist (e.g. mailinator.com, tempmail.com, guerrillamail.com). The address is
 * syntactically valid but not acceptable. Carries the offending value so
 * {@link DisposableEmailExceptionHandler} can report what was actually seen. Checked so it appears in
 * the function's {@code throws} clause and is routed as an escalation.
 */
public class DisposableEmailException extends Exception {

    private final String email;

    public DisposableEmailException(String email) {
        super("Email must not use a disposable-domain provider, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
