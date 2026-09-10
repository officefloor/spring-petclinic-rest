package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.rest.function.common.IdentityVersion;
import org.springframework.samples.petclinic.rest.function.common.Sha256;
import org.springframework.samples.petclinic.rest.function.common.Soundex;

/**
 * The single derived key used to detect duplicate owners. It is the full lower-case SHA-256 hex
 * digest over {@code normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName)}:
 *
 * <ul>
 *   <li>the telephone in canonical E.164 form (see {@link OwnerTelephone});</li>
 *   <li>the email lower-cased, or empty when absent;</li>
 *   <li>the last name folded to its {@link Soundex} code, so phonetically equal surnames collapse.</li>
 * </ul>
 *
 * <p>Creation is rejected with 409 only when a new owner's WHOLE identityKey equals an existing
 * (non-deleted) owner's — see {@link EnsureUniqueOwnerIdentity}. Because the telephone is part of
 * the key, two owners with the same last name and postcode but different telephones have different
 * keys: they are not a hard duplicate but a soft match (see {@link AssignOwnerPossibleDuplicate}).
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identityKey of a stored owner, from its telephone, email and last name. */
    public static String of(org.springframework.samples.petclinic.model.Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** The identityKey for the given telephone, email and last name. */
    public static String of(String telephone, String email, String lastName) {
        String tel = OwnerTelephone.canonical(telephone);
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
        String soundex = Soundex.of(lastName);
        // Version 2: mix the fixed 'V2' tag into the digest input so no version-1 identityKey recurs.
        return Sha256.hex(IdentityVersion.TAG + "|" + tel + "|" + mail + "|" + soundex);
    }
}
