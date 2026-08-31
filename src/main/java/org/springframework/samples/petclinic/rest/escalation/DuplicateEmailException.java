package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueOwnerEmail} when a new owner's lower-cased email already
 * belongs to another owner. Handled globally by {@link DuplicateEmailExceptionHandler},
 * which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
