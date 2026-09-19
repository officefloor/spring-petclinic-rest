package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Canonical identity of an owner for duplicate detection. All duplicate detection is
 * consolidated into this single derived key: the lower-case hex SHA-256 digest of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners are
 * duplicates only when their whole keys are equal.
 *
 * <p>A person is individuated by their telephone, email and how their last name sounds: a
 * re-registration reusing all three is a duplicate regardless of address, while two members of
 * one household with different telephones have different keys and are both allowed (the softer
 * sound-alike-plus-postcode collision is only flagged, see {@link FlagPossibleDuplicate}).
 *
 * <p>Telephone is canonicalized with {@link TelephoneNormalizer#toE164(String)}, email is
 * lower-cased and the last name is reduced to its {@link Soundex} code, so the key is computed
 * identically for an incoming request and an already-stored owner regardless of incidental
 * formatting differences.
 */
final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** Identity key of a create request. */
    static String of(OwnerFieldsDto request) {
        return of(request.getTelephone(), request.getEmail(), request.getLastName());
    }

    /** Identity key of an existing owner. */
    static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private static String of(String telephone, String email, String lastName) {
        String normalizedTelephone = TelephoneNormalizer.toE164(telephone);
        String normalizedEmail = (email == null) ? "" : email.trim().toLowerCase(Locale.ROOT);
        String raw = (normalizedTelephone == null ? "" : normalizedTelephone)
                + '|' + normalizedEmail + '|' + Soundex.encode(lastName);
        return Sha256.hex(raw);
    }
}
