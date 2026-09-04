package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (non-hard) duplicate on the owner being created. A hard duplicate — the same
 * {@link OwnerIdentityKey} as an existing owner — is already rejected with 409 by
 * {@link EnsureUniqueIdentity} before this step runs, unless the request opted into a shared household
 * with {@code sharesHousehold}. This step then looks for an existing non-deleted owner whose
 * {@code soundex(lastName)} and postcode match this owner's but whose identity key <em>differs</em>:
 * such an owner is still created, but is marked {@code possibleDuplicate = true} with
 * {@code possibleDuplicateOf} set to the matching owner's id. When no such owner exists,
 * {@code possibleDuplicate} is {@code false}.
 *
 * <p>A declared household member (created with {@code sharesHousehold} true) is never a suspected
 * duplicate, so it is never flagged.
 *
 * <p>Runs after {@link BuildOwner} (so the owner already carries the normalized E.164 telephone and
 * the postcode) and before {@link SaveOwner}.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }

        String postcode = normalizePostcode(owner.getPostcode());
        if (postcode == null) {
            return; // no postcode means it cannot share a postcode with anyone
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = OwnerIdentityKey.of(owner);

        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is ignored by the identity check
            }
            if (!postcode.equals(normalizePostcode(existing.getPostcode()))) {
                continue;
            }
            if (!soundex.equals(Soundex.of(existing.getLastName()))) {
                continue;
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                continue; // same identity key is a hard duplicate concern, not a soft match
            }
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(existing.getId());
            return;
        }
    }

    private static String normalizePostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        String trimmed = postcode.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
