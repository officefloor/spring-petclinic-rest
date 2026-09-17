package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check for create-owner, responding 409 via
 * {@link DuplicateOwnerException} on an <b>identity duplicate</b> — an existing owner with the same
 * derived {@code identityKey} (telephone, email and household, see {@link OwnerIdentities}), i.e. an
 * exact resubmission of an existing owner.
 *
 * <p>Several owners may share a household (same last name and postcode, see {@link OwnerHouseholds})
 * without opting in: household co-membership is admitted and instead governed by the membership
 * level ceiling ({@link AssignOwnerMembershipLevel}). Only an exact identity match is rejected here.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} so the request
 * telephone is already E.164 and its email lower-cased. The household component of the identity key
 * matches the {@code householdId} that {@link AssignOwnerHousehold} later stores on the owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String householdId = OwnerHouseholds.id(request.getLastName(), request.getPostcode());
        String identityKey = OwnerIdentities.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner never blocks a new one
            }
            if (identityKey.equals(OwnerIdentities.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
