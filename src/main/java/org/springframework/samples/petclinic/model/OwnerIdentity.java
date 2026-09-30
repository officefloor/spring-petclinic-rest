package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * The single definition of an owner's {@code identityKey}: a derived, read-only composite of
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is normalized to E.164 via {@link OwnerTelephones} (so a stored value and a
 * freshly created one render the same way), the email is lower-cased (empty when absent) and
 * the householdId is the deterministic {@code (lastName, postcode)} hash.
 *
 * <p>This is a display/reporting value — it is exposed as the {@code identityKey} field on the
 * owner response. It is <em>not</em> the duplicate-detection mechanism: on create, duplicates
 * are rejected by {@code EnsureUniqueOwnerIdentity} keyed purely on the {@code householdId}, so
 * two owners with the same household but different telephones now collide (unless
 * {@code sharesHousehold} is set). Do not read the dedup rule from this composite.
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
