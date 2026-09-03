package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code membershipLevelCap}: the ceiling its membership level may reach,
 * being one above the current maximum {@link OwnerMembership#level(Owner) membershipLevel} among the
 * existing members of its household (those sharing the owner's computed {@code householdId}). With no
 * existing household member the cap is left unset (null), so no cap applies.
 *
 * <p>Runs after {@link AssignHouseholdId} (so the owner's {@code householdId} is set) and before
 * {@link SaveOwner}, so {@link OwnerRepository#findAll()} returns only the owners that predate this
 * create. {@link OwnerMembership#level(Owner)} applies the recorded cap.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            String existingHousehold = OwnerIdentity.householdIdOf(
                    existing.getLastName(), existing.getPostcode());
            if (householdId.equals(existingHousehold)) {
                int level = OwnerMembership.level(existing);
                if (maxLevel == null || level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
