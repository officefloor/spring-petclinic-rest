package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * The single definition of an owner's {@code identityKey}: a derived, read-only composite of
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is normalized to E.164 via {@link OwnerTelephones} (so a stored value and a
 * freshly created one render the same way), the email is lower-cased (empty when absent) and
 * the householdId is the deterministic {@code (lastName, postcode)} hash.
 *
 * <p>This composite is the create-time duplicate key: {@code EnsureUniqueOwnerIdentity} rejects a
 * new owner only when an existing owner has the same identityKey, so two owners in the same
 * household (same lastName and postcode) but with a different telephone or email are not
 * duplicates and may coexist as household members (unless {@code sharesHousehold} shortcuts the
 * check). It is also exposed as the {@code identityKey} field on the owner response.
 *
 * <p>Lives beside {@link Owner} so the mapping layer (which returns the key) can depend on it
 * without a package cycle.
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
