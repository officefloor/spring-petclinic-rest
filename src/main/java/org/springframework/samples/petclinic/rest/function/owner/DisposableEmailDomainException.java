package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectDisposableEmailDomain} when an owner request supplies an {@code email}
 * whose domain is on the disposable-domain blocklist. Handled globally by
 * {@code DisposableEmailDomainExceptionHandler}, which responds 400.
 */
public class DisposableEmailDomainException extends Exception {

    public DisposableEmailDomainException(String domain) {
        super("Email domain '" + domain + "' is a disposable-address provider and is not accepted.");
    }
}
