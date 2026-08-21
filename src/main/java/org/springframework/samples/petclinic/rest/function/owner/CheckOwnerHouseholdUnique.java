package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.HouseholdDuplicateException;

/**
 * The household-duplicate block. The household is now keyed on the computed {@code householdId}
 * (last name + postcode), so a request that shares an existing owner's household is, by default, a
 * duplicate and is rejected with 409. Declaring {@code sharesHousehold: true} bypasses this block -
 * the owner is then created as a declared household member (and is not flagged as a suspected
 * duplicate later by {@link AssignPossibleDuplicate}).
 *
 * <p>Runs after {@link AssignHousehold} finalizes the id and before {@link CheckOwnerIdentityUnique}.
 * The two checks are distinct: this one is bypassed by {@code sharesHousehold}, whereas the identity
 * check (the full {@code telephone|email|householdId} key) never is - so a full duplicate that also
 * declares {@code sharesHousehold} still collides on its identity key.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws HouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member - bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            // The owner being created is not yet persisted, so findAll() returns only other owners.
            // A soft-deleted owner is ignored, so its household never blocks a new create.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new HouseholdDuplicateException(householdId);
            }
        }
    }
}
