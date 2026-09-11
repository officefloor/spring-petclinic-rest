package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Computes an owner's {@code identityKey}: the single derived value all duplicate detection
 * is expressed through. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
 *
 * <p>Two owners are duplicates only when their WHOLE keys are equal (see
 * {@link RequireUniqueIdentity}). Because the telephone is part of the key, two members of
 * the same household (same {@code householdId}) with different telephones have different
 * keys and are both allowed; only an exact full-key match is a conflict.
 *
 * <ul>
 * <li>telephone is normalized to E.164 form ({@link Telephones#toE164(String)}),
 * <li>email is trimmed and lower-cased, contributing empty when absent,
 * <li>householdId is the owner's shared-household identifier, empty when the owner is not
 *     part of a shared household.
 * </ul>
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identity key of an existing owner, from its stored fields. */
    public static String forOwner(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The prospective identity key of an incoming create request. */
    public static String forRequest(OwnerFieldsDto request) {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? Households.idFor(request.getLastName(), request.getAddress())
                : null;
        return build(request.getTelephone(), request.getEmail(), householdId);
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
