package org.springframework.samples.petclinic.util;

/**
 * Thrown when a supplied telephone number cannot be converted into a valid E.164
 * representation. Handled by the REST layer as a 400 Bad Request.
 */
public class InvalidTelephoneException extends RuntimeException {

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone number cannot be converted to E.164: " + rejectedValue);
    }
}
