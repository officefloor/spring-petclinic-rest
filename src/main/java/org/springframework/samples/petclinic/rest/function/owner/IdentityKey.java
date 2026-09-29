package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Hashes;
import org.springframework.samples.petclinic.rest.function.common.IdentityVersion;
import org.springframework.samples.petclinic.rest.function.common.Soundex;

/**
 * The single definition of an owner's {@code identityKey} — the one derived key that all duplicate
 * detection is expressed through. Under identity version 2 the key is the SHA-256 hex digest (64
 * lower-case hex characters) of the fixed {@code V2} version tag followed by three normalized parts,
 * all joined with {@code '|'}:
 *
 * <ol>
 * <li>the fixed {@code V2} version tag, so no value produced under version 1 is produced again,</li>
 * <li>the telephone in E.164 form (see {@link TelephoneNormalizer}),</li>
 * <li>the email lower-cased, or empty when absent (see {@link EmailNormalizer}), and</li>
 * <li>the Soundex of the last name (see {@link Soundex}), so surnames that sound alike collide.</li>
 * </ol>
 *
 * <p>A create is a duplicate — rejected with a 409 — only when its <em>whole</em> key equals an
 * existing owner's. Because the telephone is part of the key, two owners with the same last name (and
 * postcode) but different telephones have different keys and are both allowed — the second is created
 * as a soft match (see {@link AssignOwnerPossibleDuplicate}), not rejected.
 *
 * <p>Shared by {@link RequireUniqueIdentity} (which rejects a collision) and the owner mapper (which
 * returns the key), so both compute it the same way and cannot drift apart. Public because the mapper
 * lives in another package.
 */
public final class IdentityKey {

    /** The identity key for the given (raw) telephone, email and last name. */
    public static String of(String telephone, String email, String lastName) {
        String tel = TelephoneNormalizer.toE164(telephone);
        if (tel == null) {
            tel = telephone == null ? "" : telephone;
        }
        String em = EmailNormalizer.isBlank(email) ? "" : EmailNormalizer.normalize(email);
        String snd = Soundex.of(lastName);
        return Hashes.sha256Hex(IdentityVersion.TAG + "|" + tel + "|" + em + "|" + snd);
    }

    /** The identity key of an existing (persisted) owner. */
    public static String ofOwner(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private IdentityKey() {
    }
}
