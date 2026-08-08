package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's derived {@code membershipLevel} at one above the current maximum
 * membershipLevel among their existing household members (owners already sharing the
 * computed {@code householdId}). Runs after {@link AssignOwnerHousehold} sets the
 * {@code householdId} and before {@link SaveOwner}, so the owner is not yet persisted
 * and never counts itself.
 *
 * <p>With no existing household member — the owner has no {@code householdId}, or is the
 * first in its household — no cap applies and the ceiling is left null, so the mapper
 * returns the uncapped level.
 */
public class CapOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return; // a household of one: no existing member to cap against
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never compare the owner against itself
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            int level = ownerMapper.membershipLevel(existing);
            if (maxLevel == null || level > maxLevel) {
                maxLevel = level;
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
