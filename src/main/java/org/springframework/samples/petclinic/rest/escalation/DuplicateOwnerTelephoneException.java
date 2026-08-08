package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose normalized telephone is already used by
 * another owner. Handled globally by {@link DuplicateOwnerTelephoneExceptionHandler},
 * which responds 409 Conflict.
 */
public class DuplicateOwnerTelephoneException extends Exception {

    public DuplicateOwnerTelephoneException(String message) {
        super(message);
    }
}
