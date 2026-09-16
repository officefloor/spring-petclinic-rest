package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same identity — its
 * {@link org.springframework.samples.petclinic.util.IdentityKey} over normalized telephone,
 * lower-cased email and last-name Soundex — as an existing, non-deleted owner, i.e. a
 * duplicate registration. Handled by {@link DuplicateIdentityExceptionHandler}, which
 * responds 409. A request that opts in with {@code sharesHousehold} is a declared member and
 * does not trigger this.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner already registered: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
