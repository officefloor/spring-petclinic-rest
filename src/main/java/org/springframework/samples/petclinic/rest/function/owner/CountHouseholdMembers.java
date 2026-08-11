package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the read-time {@code householdSize} on an owner: the number of owners that share its
 * {@code householdId} (the SHA-256 of last name + postcode assigned by {@link AssignHousehold}),
 * counting the owner itself. Owners with no {@code householdId} are a household of one.
 *
 * <p>This backs the household factor of the membership points system (a household of 3 or more
 * adds points, see {@link org.springframework.samples.petclinic.mapper.Membership}). Runs before
 * the responder so the size is carried onto the returned DTO, mirroring
 * {@link MarkBulkSignupWarning}.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
