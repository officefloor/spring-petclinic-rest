package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single value that consolidates all
 * duplicate detection for {@code POST /api/owners}. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)},
 * where the telephone is in E.164 form (see {@link E164Telephone}) and the email is
 * lower-cased. Two owners are duplicates only when their WHOLE keys are equal, so — because
 * the telephone is part of the key — two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The identity key of a stored owner, using its E.164 telephone, email and household id. */
    public static String key(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The identity key for the given parts. The telephone is normalized to E.164 (an
     * unparseable telephone contributes empty); the email is lower-cased; a null email or
     * household id contributes an empty segment.
     */
    public static String key(String telephone, String email, String householdId) {
        String tel = E164Telephone.normalizeOrNull(telephone);
        String normalizedTelephone = tel == null ? "" : tel;
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT);
        String household = householdId == null ? "" : householdId;
        return normalizedTelephone + "|" + normalizedEmail + "|" + household;
    }
}
