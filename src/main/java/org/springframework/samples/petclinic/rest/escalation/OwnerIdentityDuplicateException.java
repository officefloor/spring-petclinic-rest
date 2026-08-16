package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when a new owner's WHOLE derived {@code identityKey}
 * (telephone|email|householdId) equals an existing owner's, i.e. it is a full duplicate.
 *
 * <p>Carries the identity key so {@link OwnerIdentityDuplicateExceptionHandler} can respond 409
 * explaining why the create was rejected.
 */
public class OwnerIdentityDuplicateException extends Exception {

    private final String identityKey;

    public OwnerIdentityDuplicateException(String identityKey) {
        super("Owner identity '" + identityKey + "' is already used by another owner");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
