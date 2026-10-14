package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when a new owner's identity key (telephone, email and household) equals an existing owner's.
 */
public class DuplicateIdentityException extends RuntimeException {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same telephone number, email and household already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
