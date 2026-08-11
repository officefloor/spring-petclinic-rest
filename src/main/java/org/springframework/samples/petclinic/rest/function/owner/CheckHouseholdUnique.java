package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdConflictException;

/**
 * The household-duplicate block for {@code POST /api/owners}: because a household is keyed on the
 * computed {@code householdId} (last name + postcode), a new owner whose householdId already belongs
 * to an existing owner is a household duplicate and is rejected with 409 — <em>unless</em> the
 * request declared {@code sharesHousehold}, which bypasses this block so the owner is created as a
 * declared household member. This is what {@code sharesHousehold} now does: it no longer builds the
 * link (the link is computed), it only waives the block.
 *
 * <p>Runs after {@link AssignHouseholdId} has assigned the shared householdId and before
 * {@link SaveOwner}, so the not-yet-saved owner is not compared against itself. The full-identity
 * duplicate check ({@link CheckOwnerIdentityUnique}) is separate and still applies to a declared
 * member, so a member repeating another member's whole identityKey is still a 409.
 */
public class CheckHouseholdUnique {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerHouseholdConflictException {
        // A declared household member bypasses the block; the identity check still guards full
        // duplicates.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new OwnerHouseholdConflictException(
                        "An owner in household " + householdId + " already exists");
            }
        }
    }
}
