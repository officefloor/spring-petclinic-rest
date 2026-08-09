package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a normalized telephone that is already used by
 * another owner. Handled globally by {@link OwnerTelephoneConflictExceptionHandler}, which
 * responds 409.
 */
public class OwnerTelephoneConflictException extends Exception {

    public OwnerTelephoneConflictException(String telephone) {
        super("Telephone already in use by another owner: " + telephone);
    }
}
