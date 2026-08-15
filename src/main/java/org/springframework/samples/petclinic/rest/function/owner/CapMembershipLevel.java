package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that caps the new owner's {@code membershipLevel} so it cannot
 * exceed one above the current maximum {@code membershipLevel} among the other members of its
 * household (owners sharing this owner's {@code householdId}). The ceiling is stored as
 * {@link Owner#setMembershipLevelCap(Integer) membershipLevelCap} so it survives the save and later
 * reads; {@link Owner#getMembershipLevel()} applies it.
 *
 * <p>Runs after {@link AssignHouseholdId} (so the householdId is set) and before {@link SaveOwner}
 * (so the owner being created is not counted among the existing members and the cap is persisted).
 * Soft-deleted owners do not count. When the owner has no household, or no existing household member,
 * no cap applies and {@code membershipLevelCap} is left {@code null}.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue;
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            int level = existing.getMembershipLevel();
            if (maxLevel == null || level > maxLevel) {
                maxLevel = level;
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
