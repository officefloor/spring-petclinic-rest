package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Caps a newly created owner's {@code membershipLevel} so it cannot exceed one above the current
 * maximum {@code membershipLevel} among the owner's existing household members (those sharing the
 * same {@code householdId}). The cap is stamped as {@code membershipLevelCap} and honoured by
 * {@link MembershipLevel#of(Owner)} whenever the owner is mapped, so it survives the round-trip to
 * the database and applies to later reads too.
 *
 * <p>Only undeclared joiners are capped. A declared household member ({@code sharesHousehold: true})
 * is left uncapped - it earns its level from its own points - whereas an owner that merely shares an
 * existing household is held to one above the household's current top level. When the owner joins no
 * existing household member the cap is left unset, so no cap applies.
 *
 * <p>Runs after {@link AssignHousehold} (the household id is finalized) and before the owner is saved.
 */
public class AssignMembershipCap {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member - not capped
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            int level = MembershipLevel.of(existing);
            if (maxLevel == null || level > maxLevel) {
                maxLevel = level;
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
