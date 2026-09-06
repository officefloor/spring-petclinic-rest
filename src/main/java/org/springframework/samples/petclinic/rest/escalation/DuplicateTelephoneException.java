package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueTelephone}
 * when the normalized telephone of an owner being created is already used by another owner.
 * Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String message) {
        super(message);
    }
}
