package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.HouseholdDuplicateException;

/**
 * Rejects a create-owner request that would join an existing owner's household — the same
 * computed {@code householdId} (derived from last name and postcode by
 * {@link AssignHousehold}) — responding 409 via {@link HouseholdDuplicateException}.
 *
 * <p>The household is keyed on {@code (lastName, postcode)}, so a second owner sharing
 * that pair is the same household. Two things bypass this block: setting
 * {@code sharesHousehold: true} (a <em>declared</em> household member, which is also not
 * flagged as a possible duplicate — see {@link AssignPossibleDuplicate}), and supplying a
 * distinct {@code email}. An email is part of the owner's {@code identityKey}, so an owner
 * that provides one is a distinguishable individual rather than an accidental re-registration
 * and may join the household. Either way a full-identity duplicate is still caught afterwards
 * by {@link EnsureUniqueIdentity}, so an owner that duplicates an existing owner's whole
 * {@code identityKey} — email and all — is still rejected.
 *
 * <p>Runs after {@link AssignHousehold} (so the built owner already carries its computed
 * {@code householdId}) and before {@link EnsureUniqueIdentity}/{@link SaveOwner}. The new
 * owner is not yet in the repository, so it is never compared against itself.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) throws HouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared member bypasses the household-duplicate block
        }
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            return; // a distinct email identifies the owner; not a household duplicate
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a household duplicate
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new HouseholdDuplicateException(householdId);
            }
        }
    }
}
