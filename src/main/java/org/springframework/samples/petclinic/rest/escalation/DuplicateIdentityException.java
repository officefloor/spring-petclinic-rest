package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckUniqueIdentity} when a create-owner request's derived {@code identityKey}
 * (normalized telephone, email and householdId joined by {@code '|'}) exactly equals an existing
 * owner's identityKey. This single key consolidates the former separate telephone, email and
 * household duplicate checks: only a whole-key match is a duplicate. Handled by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with this identity already exists: " + identityKey);
    }
}
