package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * The single definition of an owner's {@code identityKey}: the one derived value that all
 * duplicate detection is expressed through. It is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is normalized to E.164 via {@link OwnerTelephones} (so a stored value and a
 * freshly created one compare the same way), the email is lower-cased (empty when absent)
 * and the householdId is used as-is (empty when the owner is not in a shared household).
 *
 * <p>Two owners collide only when their WHOLE keys are equal. Because the telephone is part
 * of the key, two members of the same household (same householdId) with different telephones
 * have different keys and are both allowed; only an exact full-key match is a duplicate.
 *
 * <p>Lives beside {@link Owner} so both the mapping layer (which returns the key) and the
 * request-handling layer (which dedups on it) can depend on it without a package cycle.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's {@code identityKey} — see the class javadoc for the exact composition. */
    public static String of(Owner owner) {
        String telephone = OwnerTelephones.toE164OrRaw(owner.getTelephone());
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }
}
