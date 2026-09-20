package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueIdentity} when a create request's {@link IdentityKeys identity
 * key} matches an existing (non-deleted) owner's — the same telephone, email and last-name
 * Soundex — i.e. a re-registration of the same identity. Carries the colliding key for the
 * handler's message. Checked so it appears in the function's {@code throws} clause and routes to
 * an escalation.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner already exists with identity key: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
