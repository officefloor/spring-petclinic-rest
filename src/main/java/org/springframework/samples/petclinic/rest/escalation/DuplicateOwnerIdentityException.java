package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code DetectOwnerDuplicate} when a new owner's
 * {@link org.springframework.samples.petclinic.model.Owner#getIdentityKey() identityKey} already
 * belongs to an existing non-deleted owner. The identity key is the SHA-256 of
 * {@code normalizedTelephone | lowerEmail | soundex(lastName)}, so a match means the same contact
 * details and phonetically-equal surname. Handled globally by
 * {@link DuplicateOwnerIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String message) {
        super(message);
    }
}
