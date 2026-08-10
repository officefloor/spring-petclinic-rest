package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose normalized telephone is already used by another owner.
 * Handled globally by {@link OwnerTelephoneConflictExceptionHandler}, which responds 409.
 */
public class OwnerTelephoneConflictException extends Exception {

    public OwnerTelephoneConflictException(String message) {
        super(message);
    }
}
