package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the owner's {@code membershipLevel} so a new owner cannot outrank their household: the level
 * may be at most one above the current maximum {@code membershipLevel} among the existing members of
 * the same household. The household is the deterministic {@link HouseholdId} derived from last name
 * and postcode, matching {@link CountHousehold} and {@link RejectDuplicateOwner}. Runs after
 * {@link AssignMembershipLevel} has set the raw level and before the owner is saved, so
 * {@link OwnerRepository#findAll()} sees only the owners that existed before this create. With no
 * existing household member the level is left unchanged.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer level = owner.getMembershipLevel();
        if (level == null) {
            return;
        }
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        Integer maxExisting = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (!householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))) {
                continue;
            }
            Integer existingLevel = existing.getMembershipLevel();
            if (existingLevel != null && (maxExisting == null || existingLevel > maxExisting)) {
                maxExisting = existingLevel;
            }
        }
        if (maxExisting != null && level > maxExisting + 1) {
            owner.setMembershipLevel(maxExisting + 1);
        }
    }
}
