package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The single derived key that consolidates all duplicate detection for
 * {@code POST /api/owners}. An owner's {@code identityKey} is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}; a
 * create is a duplicate — and rejected with 409 — only when a new owner's WHOLE identityKey
 * equals an existing owner's. Because the telephone is part of the key, two members of the
 * same household (same {@code householdId}) with different telephones have different keys and
 * are both allowed; only an exact full-key match collides.
 *
 * <p>Each component is normalized to the same canonical form the owner is stored with, so a
 * request and the owner it would duplicate produce identical keys regardless of input format:
 * the telephone to E.164 (see {@link TelephoneE164}), the email trimmed and lower-cased, and
 * the householdId derived exactly as {@link AssignHouseholdId} assigns it.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identityKey a create request would have once its owner is built. */
    public static String forRequest(OwnerFieldsDto request) {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? AssignHouseholdId.householdId(request.getLastName(), request.getAddress())
                : null;
        return of(request.getTelephone(), request.getEmail(), householdId);
    }

    /** The identityKey of an already-stored owner. */
    public static String forOwner(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String of(String telephone, String email, String householdId) {
        return normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|"
                + normalizeHousehold(householdId);
    }

    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneE164.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    private static String normalizeHousehold(String householdId) {
        return householdId == null ? "" : householdId;
    }
}
