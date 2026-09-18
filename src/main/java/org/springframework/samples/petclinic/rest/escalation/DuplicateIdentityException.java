package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same derived identity key (normalized telephone, email
 * and household id) as an existing owner. This single key subsumes the former separate telephone,
 * email and household duplicate checks. Handled by {@link DuplicateIdentityExceptionHandler}, which
 * responds 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Identity key is already in use: " + identityKey);
    }
}
