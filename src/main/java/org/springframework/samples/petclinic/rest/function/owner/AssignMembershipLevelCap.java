package org.springframework.samples.petclinic.rest.function.owner;

import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records on a newly built owner its household level ceiling: a new member's membership
 * level may not exceed one above the current maximum level among the existing members of
 * its household, so the cap is stored as {@code max + 1}. Runs after {@link AssignHousehold}
 * (so the {@code householdId} is set) and before {@link SaveOwner} (so the new owner is not
 * yet persisted and thus not counted among the existing members). When the household has no
 * existing member the cap is left unset and no ceiling applies.
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        OptionalInt maxMemberLevel = Households.maxMemberLevel(ownerRepository, owner.getHouseholdId());
        if (maxMemberLevel.isPresent()) {
            owner.setMembershipLevelCap(maxMemberLevel.getAsInt() + 1);
        }
    }
}
