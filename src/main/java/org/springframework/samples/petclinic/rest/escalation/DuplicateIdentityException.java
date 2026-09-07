package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request duplicates an existing (non-deleted) owner's identity key —
 * the SHA-256 hex over the normalized telephone, lower-cased email and the Soundex of the last name —
 * and the request did not opt in with {@code sharesHousehold}. This is the single duplicate
 * condition. Handled as a 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identity key already exists (identityKey '" + identityKey + "')");
    }
}
