package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single duplicate check on {@code POST /api/owners}, keyed off the {@link OwnerIdentityKey} —
 * the SHA-256 digest of {@code <telephone>|<email>|<soundex(lastName)>}. Rejects with 409 via
 * {@link DuplicateIdentityException} when the new owner's whole identity key equals an existing
 * owner's — an exact duplicate.
 *
 * <p>Because the telephone is part of the key, owners with the same last name and postcode but
 * different telephones have different keys and are both allowed — such a soft match is not rejected
 * here; it is flagged instead by {@link AssignOwnerPossibleDuplicate}. There is no longer a separate
 * household-duplicate block: the single identity key is the whole duplicate rule.
 *
 * <p>Runs before {@link BuildOwner} so no owner is created on conflict. Soft-deleted owners are
 * ignored, so a re-registration after a delete is allowed.
 */
public class CheckUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new registration
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
