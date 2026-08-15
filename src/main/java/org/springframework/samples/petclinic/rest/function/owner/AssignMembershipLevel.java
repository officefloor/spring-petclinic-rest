package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the new owner's {@code membershipLevel}, applying the household level ceiling: a new owner's
 * membershipLevel cannot exceed one above the current maximum membershipLevel among their existing
 * household members — the owners already persisted that share this owner's {@code householdId}. With
 * no existing household member the level derived from points stands uncapped.
 *
 * <p>Runs after {@link AssignHouseholdId}, {@link AssignHouseholdMemberCount} and
 * {@link AssignNamesakeCount} have populated the fields the derived level reads, and before
 * {@link SaveOwner}, within the write transaction so the household maximum reflects only owners
 * already persisted (excluding this new, not-yet-saved one). The capped value is stored on the entity
 * so it is returned by the create response and later reads (see
 * {@link OwnerMapper#membershipLevel(Owner)}).
 *
 * <p>Each existing member's level is evaluated against the household's <em>current</em> size (this
 * owner's {@code householdMemberCount}, which already counts the new member): a member's own stored
 * household count is frozen at its own create time, so a household that has since grown would otherwise
 * understate the members' levels and cap the newcomer too low.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer householdSize = owner.getHouseholdMemberCount();
        int derived = OwnerMapper.derivedMembershipLevel(owner);
        String householdId = owner.getHouseholdId();
        Integer householdMax = null;
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    int level = OwnerMapper.derivedMembershipLevel(existing, householdSize);
                    if (householdMax == null || level > householdMax) {
                        householdMax = level;
                    }
                }
            }
        }
        int capped = householdMax == null ? derived : Math.min(derived, householdMax + 1);
        owner.setMembershipLevel(capped);
    }
}
