package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueIdentity} when a create request's whole
 * {@link IdentityKeys identity key} equals an existing owner's. Carries the colliding key for
 * the handler's message. Checked so it appears in the function's {@code throws} clause and
 * routes to an escalation.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Identity already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
