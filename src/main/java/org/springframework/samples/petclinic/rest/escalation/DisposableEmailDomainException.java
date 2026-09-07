package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email address whose domain is on the
 * disposable-domain blocklist (mailinator.com, tempmail.com, guerrillamail.com).
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String email, String domain) {
        super("Email domain '" + domain + "' is a disposable-domain and is not allowed: " + email);
    }
}
