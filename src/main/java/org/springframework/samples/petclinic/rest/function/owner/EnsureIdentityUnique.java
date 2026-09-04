package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate check for {@code POST /api/owners}, keyed on the whole {@code identityKey} — the
 * single source of truth for duplicate detection (see {@link OwnerIdentity}). Two owners are
 * duplicates only when their <em>whole</em> identityKey
 * ({@code normalizedTelephone + '|' + email + '|' + householdId}) is equal; because the telephone is
 * part of the key, two members of the same household (same lastName and postcode) with different
 * telephones have different identityKeys and are both allowed — which is what lets a household hold
 * more than one member. On an exact full-key match it throws {@link DuplicateIdentityException},
 * handled globally as 409. A request that sets {@code sharesHousehold} bypasses the check entirely.
 */
public class EnsureIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        // A declared household member is allowed to join an existing household, so it bypasses the
        // duplicate block entirely.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }

        String identityKey = OwnerIdentity.keyOf(request.getTelephone(), request.getEmail(),
                request.getLastName(), request.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            // A soft-deleted owner no longer blocks a create.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            String existingKey = OwnerIdentity.keyOf(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName(), existing.getPostcode());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
