package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when the supplied telephone, once normalized to its
 * digits, is already used by an existing owner.
 *
 * <p>Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String message) {
        super(message);
    }
}
