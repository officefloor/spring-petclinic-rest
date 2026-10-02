package org.springframework.samples.petclinic.rest.function.owner;

import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Applies the household level ceiling to the new owner: its
 * {@link Owner#getMembershipLevel() membershipLevel} cannot exceed one above the current maximum
 * membershipLevel among the other members of its household (owners sharing its householdId). Runs
 * after {@link BuildOwner} has resolved the owner's own household size and before it is saved, so
 * the scan sees only the existing members. Sets the owner's
 * {@link Owner#setMembershipLevelCap(Integer) membershipLevelCap} to {@code max + 1}; when the
 * household has no existing member it leaves the cap unset, so no ceiling applies. Mutates the
 * owner in place so the responder's mapper carries the capped level onto the DTO.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        OptionalInt maxMemberLevel = Household.maxMemberLevel(owner.getHouseholdId(), owner, ownerRepository);
        if (maxMemberLevel.isPresent()) {
            owner.setMembershipLevelCap(maxMemberLevel.getAsInt() + 1);
        }
    }
}
