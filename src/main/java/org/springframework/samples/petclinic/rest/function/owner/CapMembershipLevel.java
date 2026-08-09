package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's {@code membershipLevel} so it cannot exceed one above the current maximum
 * {@code membershipLevel} among the other members of this owner's household — owners that already
 * share this owner's {@code householdId}. The ceiling is {@code max + 1}; when the derived level is
 * already at or below the ceiling it is left unchanged.
 *
 * <p>When the owner has no household ({@code householdId} is null) or no existing household member,
 * no ceiling applies and the level is left as {@link AssignMembershipLevel} derived it. Members whose
 * own {@code membershipLevel} is null (e.g. seed data) and soft-deleted owners are not counted.
 *
 * <p>Runs after {@link AssignMembershipLevel} (so the derived level exists) and before
 * {@link SaveOwner}, mutating the not-yet-persisted owner in place.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return; // no household -> no ceiling
        }
        Integer level = owner.getMembershipLevel();
        if (level == null) {
            return;
        }

        Integer maxMemberLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never compare against self
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            Integer existingLevel = existing.getMembershipLevel();
            if (existingLevel == null) {
                continue;
            }
            if (maxMemberLevel == null || existingLevel > maxMemberLevel) {
                maxMemberLevel = existingLevel;
            }
        }

        if (maxMemberLevel == null) {
            return; // no existing household member -> no ceiling
        }
        int ceiling = maxMemberLevel + 1;
        if (level > ceiling) {
            owner.setMembershipLevel(ceiling);
        }
    }
}
