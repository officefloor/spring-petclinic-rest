package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single value that consolidates all
 * duplicate detection for {@code POST /api/owners}. The key is the SHA-256 hex digest (see
 * {@link Sha256}, 64 lower-case hex characters) of
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + soundex(lastName)}, where the
 * telephone is in E.164 form (see {@link E164Telephone}), the email is lower-cased and the last
 * name is folded to its Soundex code (see {@link NameSoundex}). Two owners are duplicates only
 * when their WHOLE keys are equal, so — because the telephone is part of the key — two owners
 * sharing a last name (phonetically) and postcode but carrying different telephones have
 * different keys and are both allowed; the later {@link AssignPossibleDuplicate} step merely
 * flags the second as a soft duplicate.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The identity key of a stored owner, using its E.164 telephone, email and last name. */
    public static String key(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * The identity key for the given parts: the SHA-256 hex digest of
     * {@code normalizedTelephone + '|' + email + '|' + soundex(lastName)}. The telephone is
     * normalized to E.164 (an unparseable telephone contributes empty); the email is lower-cased
     * (a null email contributes an empty segment); the last name is reduced to its Soundex code.
     */
    public static String key(String telephone, String email, String lastName) {
        String tel = E164Telephone.normalizeOrNull(telephone);
        String normalizedTelephone = tel == null ? "" : tel;
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT);
        String soundex = NameSoundex.of(lastName);
        return Sha256.hex(normalizedTelephone + "|" + normalizedEmail + "|" + soundex);
    }
}
