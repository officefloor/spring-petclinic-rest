package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a normalized telephone already held by
 * another owner. Handled by {@link DuplicateOwnerTelephoneExceptionHandler}, which
 * responds 409.
 */
public class DuplicateOwnerTelephoneException extends Exception {

    public DuplicateOwnerTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
    }
}
