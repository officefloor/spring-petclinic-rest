package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's {@code identityKey}: a derived, human-readable summary of the fields
 * that identify an owner. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
 *
 * <p>Household membership itself is enforced separately by {@link RequireUniqueIdentity},
 * which rejects a second owner in the same household (same {@code householdId}, derived from
 * lastName + postcode) unless it declares {@code sharesHousehold}.
 *
 * <ul>
 * <li>telephone is normalized to E.164 form ({@link Telephones#toE164(String)}),
 * <li>email is trimmed and lower-cased, contributing empty when absent,
 * <li>householdId is the owner's deterministic household identifier (from lastName +
 *     postcode), empty only when it has not been assigned.
 * </ul>
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identity key of an existing owner, from its stored fields. */
    public static String forOwner(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String build(String telephone, String email, String householdId) {
        String tel = Telephones.toE164(telephone);
        return (tel == null ? "" : tel) + "|" + normalizeEmail(email) + "|"
                + (householdId == null ? "" : householdId);
    }

    /** Trim and lower-case; null or blank yields empty (no email to contribute). */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
