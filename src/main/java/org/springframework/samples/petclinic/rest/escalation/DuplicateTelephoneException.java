package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckUniqueTelephone} when a create-owner request's normalized telephone
 * (10 digits, non-digit characters stripped by {@code ValidateOwnerFields}) is already used by
 * another owner. Handled by {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use by another owner: " + telephone);
    }
}
