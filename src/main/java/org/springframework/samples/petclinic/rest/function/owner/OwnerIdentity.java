package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey}: the one derived value that all
 * create-time duplicate detection for {@code POST /owners} is expressed through. It consolidates
 * what used to be three separate guards (telephone, email and household) into a single key, so a
 * request is a duplicate only when its <em>whole</em> key equals an existing owner's.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or
 * empty)}:
 * <ul>
 *   <li>telephone is canonicalised to E.164 ({@link OwnerTelephone#toE164(String)}) so the
 *       comparison is independent of how a value happens to be formatted;</li>
 *   <li>email is the stored lower-cased form ({@link OwnerEmail#normalize(String)}) when present,
 *       otherwise the empty string (email is optional);</li>
 *   <li>householdId is the owner's shared-household identifier when it carries one, otherwise the
 *       empty string.</li>
 * </ul>
 *
 * <p>Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed; only an
 * exact full-key match counts as a duplicate.
 *
 * <p>Centralised here so {@link CheckOwnerIdentityUnique} (which rejects collisions) and the owner
 * mapper (which returns the key) cannot disagree on how it is derived.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The identity key of an existing owner, derived from its stored fields. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The identity key for the given parts: {@code normalizedTelephone + '|' + (email or empty) +
     * '|' + (householdId or empty)}.
     */
    public static String key(String telephone, String email, String householdId) {
        return normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|"
                + (householdId == null ? "" : householdId);
    }

    private static String normalizeTelephone(String telephone) {
        String e164 = OwnerTelephone.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }

    private static String normalizeEmail(String email) {
        return OwnerEmail.isPresent(email) ? OwnerEmail.normalize(email) : "";
    }
}
