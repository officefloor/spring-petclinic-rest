package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's derived identity key — the normalized telephone, the
 * email (or empty) and the household id (or empty) — matches, in whole, an existing owner's.
 * This is the single duplicate condition, replacing the former separate telephone, email and
 * household checks. Handled as a 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identity already exists (identityKey '" + identityKey + "')");
    }
}
