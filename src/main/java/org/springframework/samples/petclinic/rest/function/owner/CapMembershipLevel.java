package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MembershipLevels;

/**
 * Caps a new owner's membership level at one above the highest membership level among their
 * existing household members — those sharing its {@link HouseholdKey household id}. Runs after
 * {@link AssignHouseholdId} has set the id and before {@link SaveOwner} persists the entity, so
 * {@code findAll()} sees only the owners that existed before this create. When the household has
 * no existing member, no cap applies and the level is left uncapped. The cap is stored on the
 * owner so every later read derives the same capped level.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        LocalDate asOf = LocalDate.now();
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is no longer a household member
            }
            if (householdId.equals(existing.getHouseholdId())) {
                int level = MembershipLevels.levelOf(existing, asOf);
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
