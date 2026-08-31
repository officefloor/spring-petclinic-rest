package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueOwnerTelephone} when a new owner's normalized telephone
 * already belongs to another owner. Handled globally by {@link DuplicateTelephoneExceptionHandler},
 * which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String message) {
        super(message);
    }
}
