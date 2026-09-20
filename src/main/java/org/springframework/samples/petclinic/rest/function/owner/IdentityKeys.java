package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single derived key that governs owner duplicate detection. An owner's
 * {@code identityKey} is {@code normalizedTelephone + "|" + (email or empty) + "|" +
 * householdId}, canonicalizing each part the same way it is stored (E.164 telephone,
 * lower-cased email, shared household id). Two owners are duplicates only when their
 * <em>whole</em> keys are equal, so the previously separate telephone, email and household
 * checks are now three parts of one comparison: members of the same household with
 * different telephones have different keys and are both allowed.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /**
     * The identity key of an existing owner, derived from its stored fields.
     */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The identity key a create request resolves to, using the household id the owner would
     * be assigned (see {@link Households#resolveHouseholdId}).
     */
    static String of(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        return key(request.getTelephone(), request.getEmail(),
                Households.resolveHouseholdId(request, ownerRepository));
    }

    private static String key(String telephone, String email, String householdId) {
        String normalizedTelephone = telephone == null ? ""
                : Telephones.toE164(telephone).orElse(telephone);
        String normalizedEmail = email == null || email.isBlank() ? "" : Emails.normalize(email);
        String household = householdId == null ? "" : householdId;
        return normalizedTelephone + "|" + normalizedEmail + "|" + household;
    }
}
