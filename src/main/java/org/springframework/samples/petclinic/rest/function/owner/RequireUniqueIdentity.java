package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Step of {@code POST /api/owners} that rejects a create whose derived identity (see
 * {@link OwnerIdentity}) collides with an existing owner's, throwing {@link DuplicateIdentityException}
 * (handled as 409 Conflict). This is the single, consolidated duplicate check that replaces the
 * former separate telephone, email and household checks: they are all expressed through the one
 * {@code identityKey}. Because the telephone is part of the key, two members of the same household
 * (same householdId) with different telephones are distinct contacts and are both allowed; only two
 * owners that are the same contact (matching telephone and email) collide.
 *
 * <p>Runs after {@link RequireOwnerFields} has normalized and published the body and before
 * {@link BuildOwner}/{@link SaveOwner} persist the new owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String contactKey = OwnerIdentity.contactKey(request.getTelephone(), request.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            String existingKey = OwnerIdentity.contactKey(existing.getTelephone(), existing.getEmail());
            if (contactKey.equals(existingKey)) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity already exists");
            }
        }
    }
}
