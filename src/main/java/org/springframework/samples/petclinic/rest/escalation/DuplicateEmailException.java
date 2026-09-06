package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueEmail}
 * when the lower-cased email of an owner being created is already used by another owner.
 * Handled by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
