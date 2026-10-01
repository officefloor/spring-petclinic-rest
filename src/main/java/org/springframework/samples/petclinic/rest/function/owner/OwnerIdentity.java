package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Single source of truth for an owner's derived {@code identityKey} — the one value that now
 * expresses all duplicate detection for the {@code POST /api/owners} pipeline. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the telephone is
 * reduced to E.164 through {@link OwnerTelephone#toE164} and the email lower-cased through
 * {@link OwnerEmail#normalize}; a missing email or household id contributes an empty segment.
 *
 * <p>Two owners are duplicates only when their <em>whole</em> identity keys are equal. Because the
 * telephone is part of the key, two members of the same household (same {@code householdId}) with
 * different telephones have different keys and are both allowed — only an exact full-key match is a
 * duplicate. Used by {@link CheckOwnerIdentityUnique} to reject a collision and by the owner mapper
 * to return the key on the owner.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Builds the identity key from its three raw parts: the submitted/stored telephone (normalized
     * to E.164 here), the email (lower-cased here) and the already-derived household id. Any part
     * that is {@code null} or cannot normalize contributes an empty segment.
     */
    public static String identityKey(String telephone, String email, String householdId) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        String normalizedEmail = OwnerEmail.normalize(email);
        return (normalizedTelephone == null ? "" : normalizedTelephone) + "|"
                + (normalizedEmail == null ? "" : normalizedEmail) + "|"
                + (householdId == null ? "" : householdId);
    }

    /**
     * The identity key of an already-stored owner, read from its persisted fields. The single
     * {@code Owner}-to-key definition shared by {@link CheckOwnerIdentityUnique} and the owner
     * mapper, so the uniqueness check and the returned {@code identityKey} can never drift apart.
     */
    public static String identityKey(Owner owner) {
        return identityKey(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The identity key a create request <em>would</em> produce. The household-id segment is derived
     * the same way {@link AssignOwnerHousehold} will later assign it: the stable
     * {@link OwnerHousehold#householdId} when the request opts in with {@code sharesHousehold: true},
     * otherwise empty.
     */
    static String forRequest(OwnerFieldsDto request) {
        String householdId = Boolean.TRUE.equals(request.getSharesHousehold())
                ? OwnerHousehold.householdId(request.getLastName(), request.getAddress())
                : "";
        return identityKey(request.getTelephone(), request.getEmail(), householdId);
    }
}
