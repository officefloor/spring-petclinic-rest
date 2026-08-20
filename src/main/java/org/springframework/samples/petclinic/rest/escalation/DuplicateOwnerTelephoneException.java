package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerTelephoneUnique} when a create-owner request carries a
 * normalized telephone that is already used by another owner. Handled by
 * {@link DuplicateOwnerTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerTelephoneException extends Exception {

    public DuplicateOwnerTelephoneException(String telephone) {
        super("Telephone is already used by another owner: " + telephone);
    }
}
