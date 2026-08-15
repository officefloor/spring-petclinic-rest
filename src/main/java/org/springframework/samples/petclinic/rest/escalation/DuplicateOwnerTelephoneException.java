package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's normalized telephone is already used by
 * another owner. Handled globally by {@link DuplicateOwnerTelephoneExceptionHandler}, which
 * responds 409.
 */
public class DuplicateOwnerTelephoneException extends Exception {

    public DuplicateOwnerTelephoneException(String telephone) {
        super("An owner with telephone " + telephone + " already exists");
    }
}
