package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by
 * {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerIdentityUnique} when the
 * owner being created has the same derived {@code identityKey} (normalizedTelephone + '|' + email +
 * '|' + householdId) as an existing owner. This single check replaces the former separate telephone,
 * email and household duplicate checks. Handled by {@link OwnerIdentityDuplicateExceptionHandler},
 * which responds 409.
 */
public class OwnerIdentityDuplicateException extends Exception {

    private final String identityKey;

    public OwnerIdentityDuplicateException(String identityKey) {
        super("Owner identity already in use by another owner: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
