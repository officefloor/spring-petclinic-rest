package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a new owner's email domain is on the disposable-domain blocklist.
 * Handled by {@link DisposableEmailExceptionHandler} as 400.
 */
public class DisposableEmailException extends Exception {

    public DisposableEmailException(String email) {
        super("Disposable email domain not allowed: " + email);
    }
}
