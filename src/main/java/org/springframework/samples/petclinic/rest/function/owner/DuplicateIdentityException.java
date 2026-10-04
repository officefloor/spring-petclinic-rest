package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerIdentityUnique} when an owner create request's whole
 * {@link OwnerIdentity identityKey} equals an existing owner's — i.e. the request matches on
 * normalised telephone, email and household together. Checked so it appears in the
 * {@code throws} clause and is routed to its escalation handler, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with this identity already exists: " + identityKey);
    }
}
