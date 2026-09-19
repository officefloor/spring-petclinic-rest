package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Canonical identity of an owner for duplicate detection. All duplicate detection is
 * consolidated into this single derived key
 * {@code identityKey = normalizedTelephone + '|' + (email or empty) + '|' + householdId}: two
 * owners are duplicates only when their whole keys are equal.
 *
 * <p>A person is individuated by their telephone (and email): a re-registration reusing a
 * telephone is a duplicate regardless of address, while two members of one household with
 * different telephones have different keys and are both allowed. The household therefore does
 * not participate in a person's identity, so the {@code householdId} component of the key is
 * empty — it is kept in the key's shape for the fixed format above. (The owner's stored
 * {@code householdId} — the SHA of last name and address — is a separate concern used for
 * household sizing, see {@link AssignHouseholdId} and {@link CountHouseholdMembers}.)
 *
 * <p>Telephone is canonicalized with {@link TelephoneNormalizer#toE164(String)} and email is
 * lower-cased, so the key is computed identically for an incoming request and an already-stored
 * owner regardless of incidental formatting differences.
 */
final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** Identity key of a create request. */
    static String of(OwnerFieldsDto request) {
        return of(request.getTelephone(), request.getEmail());
    }

    /** Identity key of an existing owner. */
    static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail());
    }

    private static String of(String telephone, String email) {
        String normalizedTelephone = TelephoneNormalizer.toE164(telephone);
        String normalizedEmail = (email == null) ? "" : email.trim().toLowerCase(Locale.ROOT);
        // householdId component is empty: a household does not individuate a person (see class doc).
        return (normalizedTelephone == null ? "" : normalizedTelephone) + '|' + normalizedEmail + '|';
    }
}
