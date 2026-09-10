package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueOwnerTelephone} when a create-owner request's normalized
 * telephone is already used by an existing owner. Handled by
 * {@code DuplicateOwnerTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerTelephoneException extends Exception {

    public DuplicateOwnerTelephoneException(String telephone) {
        super("Telephone already in use by another owner: '" + telephone + "'");
    }
}
