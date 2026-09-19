package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request repeats an existing live owner's identity key (the same
 * normalized telephone, email and phonetically-equal last name) without opting in to sharing a
 * household. An owner that merely sounds like an existing one at the same postcode is not a
 * duplicate — it is created and flagged as a possible duplicate instead. Handled by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner already exists with this identity: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
