package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Collection;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects a create-owner request whose derived {@link OwnerIdentity identity key} exactly
 * matches an existing owner's, responding 409. This is the single duplicate check: the
 * former separate telephone, email and household checks are all expressed through the one
 * key (see {@link OwnerIdentity}), so two owners collide only when their <em>whole</em> key
 * matches. Because the telephone is part of the key, members of the same household with
 * different telephones have different keys and are both allowed.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail}, so the
 * request's telephone and email are in stored form; the household id is the one the request
 * would be assigned (see {@link OwnerHouseholds#assignedId}), matching what
 * {@link AssignOwnerHousehold} later stores.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        Collection<Owner> existing = ownerRepository.findAll();
        String householdId = OwnerHouseholds.assignedId(Boolean.TRUE.equals(request.getSharesHousehold()),
                request.getLastName(), request.getAddress(), existing);
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner owner : existing) {
            if (identityKey.equals(OwnerIdentity.of(owner))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
