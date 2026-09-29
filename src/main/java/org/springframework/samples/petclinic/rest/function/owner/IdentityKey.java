package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner's {@code identityKey} — the one derived key that all duplicate
 * detection is expressed through. The key joins three normalized parts with {@code '|'}:
 *
 * <ol>
 * <li>the telephone in E.164 form (see {@link TelephoneNormalizer}),</li>
 * <li>the email lower-cased, or empty when absent (see {@link EmailNormalizer}), and</li>
 * <li>the {@code householdId}, or empty when the owner does not share a household.</li>
 * </ol>
 *
 * <p>A create is a duplicate — rejected with a 409 — only when its <em>whole</em> key equals an
 * existing owner's. Because the telephone is part of the key, two members of one household (the same
 * {@code householdId}) with different telephones have different keys and are both allowed.
 *
 * <p>Shared by {@link RequireUniqueIdentity} (which rejects a collision) and the owner mapper (which
 * returns the key), so both compute it the same way and cannot drift apart. Public because the mapper
 * lives in another package.
 */
public final class IdentityKey {

    /** The identity key for the given (raw) telephone, email and household id. */
    public static String of(String telephone, String email, String householdId) {
        String tel = TelephoneNormalizer.toE164(telephone);
        if (tel == null) {
            tel = telephone == null ? "" : telephone;
        }
        String em = EmailNormalizer.isBlank(email) ? "" : EmailNormalizer.normalize(email);
        String hh = householdId == null ? "" : householdId;
        return tel + "|" + em + "|" + hh;
    }

    /** The identity key of an existing (persisted) owner. */
    public static String ofOwner(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private IdentityKey() {
    }
}
