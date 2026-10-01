package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdDuplicateException;

/**
 * Household duplicate-detection step of {@code POST /api/owners}. Runs after {@link AssignHousehold}
 * (so the new owner's deterministic {@code householdId} is set) and before
 * {@link CheckOwnerIdentityUnique}.
 *
 * <p>Because a household is now keyed on {@code (lastName, postcode)}, two owners sharing a last name
 * and postcode are the same household. A second such owner is rejected with a 409 via
 * {@link OwnerHouseholdDuplicateException} — <em>unless</em> the request declares the shared household
 * via {@code sharesHousehold}, in which case the new owner is accepted as a declared household member.
 * The {@code sharesHousehold} flag only bypasses this block; it no longer creates the household link,
 * which is derived automatically from the computed {@code householdId}.
 *
 * <p>An owner with no postcode has a {@code null} {@code householdId} (no household to key on) and so is
 * never a household duplicate. The new owner is still unsaved, so it is not among the owners iterated
 * here and cannot match itself.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return; // no postcode, so no household to collide with
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new create
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new OwnerHouseholdDuplicateException(householdId);
            }
        }
    }
}
