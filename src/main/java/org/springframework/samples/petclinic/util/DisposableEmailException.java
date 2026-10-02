package org.springframework.samples.petclinic.util;

/**
 * Thrown when a supplied email address belongs to a disposable-email domain on the
 * blocklist. Handled by the REST layer as a 400 Bad Request.
 */
public class DisposableEmailException extends RuntimeException {

    public DisposableEmailException(String email) {
        super("Email '" + email + "' uses a disposable-email domain that is not accepted");
    }
}
