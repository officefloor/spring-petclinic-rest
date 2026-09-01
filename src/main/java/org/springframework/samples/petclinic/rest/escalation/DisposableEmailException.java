package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureOwnerEmailNotDisposable} when a new owner's email domain is on the
 * disposable-domain blocklist. Handled globally by {@link DisposableEmailExceptionHandler},
 * which responds 400.
 */
public class DisposableEmailException extends Exception {

    public DisposableEmailException(String message) {
        super(message);
    }
}
