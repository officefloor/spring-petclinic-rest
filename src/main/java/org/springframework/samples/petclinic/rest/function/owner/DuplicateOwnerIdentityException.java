package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueOwnerIdentity} when a create-owner request would be a second
 * member of an existing household (the deterministic {@code householdId} for its lastName and
 * postcode is already used by another owner) and the request did not declare
 * {@code sharesHousehold}. Handled by {@code DuplicateOwnerIdentityExceptionHandler}, which
 * responds 409.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String householdId) {
        super("Another owner already belongs to this household: '" + householdId + "'");
    }
}
