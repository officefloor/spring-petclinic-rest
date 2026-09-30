package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * The single definition of an owner's {@code identityKey}: the lower-case hex SHA-256 over
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + soundex(lastName)}, where the
 * telephone is normalized to E.164 via {@link OwnerTelephones} (so a stored value and a
 * freshly created one render the same way), the email is lower-cased (empty when absent) and
 * the surname is folded to its American Soundex code by {@link Soundex} (so names that sound
 * alike share the surname component). The key is therefore a fixed 64-character hex string.
 *
 * <p>This composite is the create-time duplicate key: {@code EnsureUniqueOwnerIdentity} rejects a
 * new owner only when an existing owner has the same identityKey. Because the telephone is part
 * of the key, two owners with the same lastName and postcode but a <em>different</em> telephone
 * are not identity duplicates and may coexist — they are soft-flagged as a possible duplicate
 * instead. The householdId (the {@code (lastName, postcode)} hash) is no longer part of the key.
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
        String surname = Soundex.of(owner.getLastName());
        return Hashing.sha256Hex(telephone + "|" + email + "|" + surname);
    }
}
