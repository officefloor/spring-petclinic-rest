package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the household level ceiling for a new owner: a new owner's {@code membershipLevel}
 * cannot exceed one above the current maximum {@code membershipLevel} among the other members of
 * its household (owners that share its {@code householdId}). The ceiling is stored on the owner as
 * {@code membershipLevelCap} = {@code maxExistingLevel + 1}; {@link OwnerMapper#membershipLevel}
 * clamps the derived level down to it at read time.
 *
 * <p>Runs within the create transaction after {@link AssignHousehold}/{@link AssignHouseholdSize}
 * (so the {@code householdId} is set) and before {@link SaveOwner} (so the owner being created is
 * not yet persisted and is therefore not counted among its own household members). When the owner
 * belongs to no household, or no other member of its household yet exists, no cap is recorded and
 * the owner keeps its uncapped level.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return; // no household -> no cap applies
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing == owner || !householdId.equals(existing.getHouseholdId())) {
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
