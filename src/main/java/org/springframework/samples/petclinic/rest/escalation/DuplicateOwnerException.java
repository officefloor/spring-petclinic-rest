package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same {@code identityKey} as an existing non-deleted owner
 * — the SHA-256 over the normalized telephone, email and soundex of the last name — and did not opt
 * into a shared household via {@code sharesHousehold}. Handled by {@link DuplicateOwnerExceptionHandler},
 * which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String identityKey) {
        super("An owner with the same identity already exists: " + identityKey);
    }
}
