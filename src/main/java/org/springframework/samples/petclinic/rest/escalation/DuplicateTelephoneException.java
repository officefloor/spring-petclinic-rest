package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerTelephoneUnique} when the normalized telephone of a new owner
 * is already used by an existing owner. Handled globally by
 * {@link DuplicateTelephoneExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String message) {
        super(message);
    }
}
