package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey} — a derived value returned on the
 * owner DTO. The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId},
 * where the {@code householdId} is now deterministic from the owner's household (last name and
 * postcode — see {@link HouseholdKey}).
 *
 * <p>The three parts are normalized the same way they are stored: the telephone to its E.164
 * form (see {@link OwnerTelephones}, so a stored value that predates the pipeline still
 * compares correctly), the email trimmed and lower-cased (empty when absent), and the
 * {@code householdId} as stored (empty when absent). Duplicate detection itself is keyed on the
 * household (see {@link RejectDuplicateIdentity}); this key is exposed by the owner mapper.
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
