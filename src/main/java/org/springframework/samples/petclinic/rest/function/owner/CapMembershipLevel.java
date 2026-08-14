package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that captures the owner's household-capped {@code membershipLevel}.
 *
 * <p>The owner's naturally {@link MembershipLevel#of(Owner) computed} level cannot exceed one above
 * the current maximum {@link MembershipLevel#effective(Owner) effective} level among the existing
 * members of its household (owners sharing the same {@code householdId}, which is not yet the owner
 * being created). When the owner has no household or has no existing household member, no cap applies
 * and the natural level is stored verbatim.
 *
 * <p>Runs after {@link AssignHouseholdSize} and {@link AssignNamesakeCount} so the inputs the natural
 * level depends on (household size, namesake count, email, registration date) are finalized, and
 * before {@link SaveOwner} so the captured level is persisted with the owner. The stored value is what
 * {@code OwnerMapper} later returns as {@code membershipLevel}.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int natural = MembershipLevel.of(owner);
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setMembershipLevel(natural);
            return;
        }
        Integer maxExisting = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                int level = MembershipLevel.effective(existing);
                if (maxExisting == null || level > maxExisting) {
                    maxExisting = level;
                }
            }
        }
        owner.setMembershipLevel(maxExisting == null ? natural : Math.min(natural, maxExisting + 1));
    }
}
