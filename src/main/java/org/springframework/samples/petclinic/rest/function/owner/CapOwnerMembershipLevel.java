package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps a newly created owner's {@code membershipLevel} so it cannot exceed one above the current
 * maximum {@code membershipLevel} among their existing household members.
 *
 * <p>Household members are the already-persisted owners that share this owner's {@code householdId}
 * (soft-deleted owners are ignored). The owner being created is not yet persisted, so
 * {@code findAll} returns only the existing members. When the owner belongs to no household, or no
 * existing member carries a {@code membershipLevel}, no cap applies and the level computed by
 * {@link AssignOwnerMembershipLevel} stands unchanged.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link AssignOwnerMembershipLevel} (so the
 * uncapped level is already set) and before {@link SaveOwner} (so the capped value is persisted and
 * later read back unchanged).
 */
public class CapOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer level = owner.getMembershipLevel();
        String householdId = owner.getHouseholdId();
        if (level == null || householdId == null) {
            return; // no level to cap, or no household so no cap applies
        }
        Integer maxExisting = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are not household members
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            Integer existingLevel = existing.getMembershipLevel();
            if (existingLevel != null && (maxExisting == null || existingLevel > maxExisting)) {
                maxExisting = existingLevel;
            }
        }
        if (maxExisting == null) {
            return; // no existing household member, so no cap applies
        }
        int ceiling = maxExisting + 1;
        if (level > ceiling) {
            owner.setMembershipLevel(ceiling);
        }
    }
}
