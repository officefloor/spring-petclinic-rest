package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey}: the one derived value that all
 * create-time duplicate detection for {@code POST /owners} is expressed through. It consolidates
 * what used to be several separate guards (telephone, email and household) into a single key, so a
 * request is a duplicate only when its <em>whole</em> key equals an existing owner's.
 *
 * <p>The key is the lower-case hex SHA-256 of {@code normalizedTelephone + '|' + lowerEmail + '|' +
 * soundex(lastName)}:
 * <ul>
 *   <li>telephone is canonicalised to E.164 ({@link OwnerTelephone#toE164OrRaw(String)}) so the
 *       comparison is independent of how a value happens to be formatted;</li>
 *   <li>email is the stored lower-cased form ({@link OwnerEmail#normalize(String)}) when present,
 *       otherwise the empty string (email is optional);</li>
 *   <li>last name contributes its Soundex code ({@link OwnerSoundex#of(String)}), so surnames that
 *       sound alike share that component.</li>
 * </ul>
 *
 * <p>Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones have different keys and are both allowed (the later pipeline flags the second
 * as a <em>possible</em> duplicate, see {@link AssignPossibleDuplicate}); only an exact full-key
 * match counts as a duplicate.
 *
 * <p>Centralised here so {@link CheckOwnerIdentityUnique} (which rejects collisions) and the owner
 * mapper (which returns the key) cannot disagree on how it is derived.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The identity key of an existing owner, derived from its stored fields. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * The identity key for the given parts: the lower-case hex SHA-256 of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
     */
    public static String key(String telephone, String email, String lastName) {
        String raw = OwnerTelephone.toE164OrRaw(telephone) + "|" + normalizeEmail(email) + "|"
                + OwnerSoundex.of(lastName);
        return Sha256.hex(raw);
    }

    private static String normalizeEmail(String email) {
        return OwnerEmail.isPresent(email) ? OwnerEmail.normalize(email) : "";
    }
}
