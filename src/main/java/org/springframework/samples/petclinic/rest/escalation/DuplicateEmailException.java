package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerEmailUnique} when the lower-cased email of a new owner
 * is already used by an existing owner. Handled globally by
 * {@link DuplicateEmailExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
