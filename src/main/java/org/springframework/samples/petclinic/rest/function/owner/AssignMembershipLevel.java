package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code membershipLevel}, applying the household ceiling: a new owner's level
 * (see {@link MembershipLevels#of(Owner)}) may not exceed one above the current maximum level among
 * their existing household members. With no existing household member the level is stored
 * uncapped.
 *
 * <p>Existing members are those sharing the new owner's computed {@code householdId} (keyed on
 * lastName + postcode; see {@link AssignHousehold}). Each is valued as a member of the household
 * the new owner is joining, i.e. at the household's current size — the new owner's
 * {@code householdSize}, which already counts the whole household — rather than the possibly stale
 * size stored when that member was itself created.
 *
 * <p>Runs after {@link AssignHouseholdSize} and {@link AssignNamesakeCount} (so the point factors
 * are set) and before {@code save} (so the new owner is not counted among the existing members).
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int level = MembershipLevels.of(owner);
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            int householdSize = owner.getHouseholdSize() == null ? 0 : owner.getHouseholdSize();
            Integer maxExisting = null;
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner no longer counts as a household member
                }
                if (householdId.equals(existing.getHouseholdId())) {
                    int existingLevel = MembershipLevels.of(existing, householdSize);
                    if (maxExisting == null || existingLevel > maxExisting) {
                        maxExisting = existingLevel;
                    }
                }
            }
            if (maxExisting != null) {
                level = Math.min(level, maxExisting + 1);
            }
        }
        owner.setMembershipLevel(level);
    }
}
