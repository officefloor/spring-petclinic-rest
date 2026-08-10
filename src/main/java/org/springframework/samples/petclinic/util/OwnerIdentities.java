package org.springframework.samples.petclinic.util;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone;

/**
 * Derives an owner's {@code identityKey}: the single value all create-time duplicate detection is
 * expressed through. The key is
 * {@code normalizedTelephone + "|" + (email or empty) + "|" + (householdId or empty)}, so two
 * owners are duplicates only when their <em>whole</em> key is equal. Because the telephone is part
 * of the key, two members of the same household (same {@code householdId}) with different telephones
 * have different keys and are both allowed; only an exact full-key match is a duplicate.
 *
 * <p>Each component is normalized the same way for every owner so the comparison is stable:
 * the telephone to E.164 (see {@link OwnerTelephone}), the email trimmed and lower-cased, and a
 * blank or absent email/householdId represented as the empty string. Normalization is idempotent,
 * so the returned key equals the one used for comparison.
 */
public final class OwnerIdentities {

    private OwnerIdentities() {
    }

    public static String identityKey(Owner owner) {
        return normalizeTelephone(owner.getTelephone()) + "|"
                + normalizeEmail(owner.getEmail()) + "|"
                + normalizeHouseholdId(owner.getHouseholdId());
    }

    private static String normalizeTelephone(String telephone) {
        try {
            return OwnerTelephone.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            // A value that cannot form a valid E.164 number cannot collide with one that can;
            // represent it by its raw form (or empty when absent) so comparison stays total.
            return telephone == null ? "" : telephone;
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase(Locale.ROOT);
    }

    private static String normalizeHouseholdId(String householdId) {
        return (householdId == null || householdId.isBlank()) ? "" : householdId;
    }
}
