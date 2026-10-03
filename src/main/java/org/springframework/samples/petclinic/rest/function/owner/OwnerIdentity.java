package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey} — the one derived value that all
 * duplicate detection is now expressed through. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, so two owners are
 * duplicates only when their WHOLE keys match: two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed.
 *
 * <p>The three parts are normalized the same way they are stored: the telephone to its E.164
 * form (see {@link OwnerTelephones}, so a stored value that predates the pipeline still
 * compares correctly), the email trimmed and lower-cased (empty when absent), and the
 * {@code householdId} as stored (empty when absent). Used by {@link RejectDuplicateIdentity}
 * to find collisions and by the owner mapper to return the key.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's duplicate-detection identity key. */
    public static String identityKey(Owner owner) {
        String telephone = OwnerTelephones.toE164(owner.getTelephone());
        if (telephone == null) {
            telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String email = owner.getEmail();
        email = (email == null || email.isBlank()) ? "" : email.trim().toLowerCase(Locale.ROOT);
        String household = owner.getHouseholdId();
        household = household == null ? "" : household;
        return telephone + "|" + email + "|" + household;
    }
}
