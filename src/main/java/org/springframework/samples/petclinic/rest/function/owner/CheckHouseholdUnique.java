package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.HouseholdDuplicateException;

/**
 * The single duplicate check for a new owner, keyed on the household. Because the {@code householdId} is
 * now deterministic from the (lastName, postcode) pair (see {@link Household}), two owners that share a
 * last name and postcode are the same household. A new owner whose computed household id already belongs
 * to an existing owner is therefore rejected with a 409 Conflict ({@link HouseholdDuplicateException}).
 *
 * <p>The {@code sharesHousehold} request flag bypasses this block: when set, the owner is admitted as a
 * declared member of the household rather than rejected (it is then not treated as a suspected duplicate
 * — see {@link FlagPossibleDuplicate}). The flag only lifts the block; it does not affect the derived
 * {@code householdId}, which is assigned deterministically by {@link AssignHousehold}.
 *
 * <p>Runs after {@link ValidateNewOwner} and {@link ValidateOwnerPostcode} (so the last name is present
 * and the postcode has been validated) and before {@link BuildOwner}. Existing owners are compared by
 * their household id recomputed from their own last name and postcode, so seed data whose
 * {@code householdId} was never assigned still collides with an equivalent request.
 */
public class CheckHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws HouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        for (Owner existing : Household.membersOf(request.getLastName(), request.getPostcode(), ownerRepository)) {
            if (!existing.isDeleted()) { // a soft-deleted owner no longer blocks a matching household
                throw new HouseholdDuplicateException(Household.id(request.getLastName(), request.getPostcode()));
            }
        }
    }
}
