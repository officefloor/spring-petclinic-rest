package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Step of {@code POST /api/owners}, the duplicate block, that rejects a create whose derived identity
 * (see {@link OwnerIdentity#key(String, String, String) identity key}) equals an existing owner's,
 * throwing {@link DuplicateIdentityException} (handled as 409 Conflict). Duplicate detection is now
 * this single identity key: two owners collide only when their telephone, email and surname phonetic
 * all match. There is no separate household-duplicate rejection — two owners sharing a surname and
 * postcode but with different telephones have different keys, so they are permitted here and are
 * instead flagged by {@link FlagPossibleDuplicate} as possible duplicates.
 *
 * <p>Soft-deleted owners are ignored, so they never block a create. The email-domain blocklist is
 * applied earlier, by {@link RequireOwnerFields}, so a blocked address is rejected before this step.
 *
 * <p>Runs after {@link RequireOwnerFields} has normalized and published the body and before
 * {@link BuildOwner}/{@link SaveOwner} persist the new owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            String existingKey = OwnerIdentity.key(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity already exists");
            }
        }
    }
}
