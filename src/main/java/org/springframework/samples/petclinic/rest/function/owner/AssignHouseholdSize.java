package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records how many owners belong to this owner's household —
 * the owners sharing its {@code householdId} — once this create is accounted for, storing the total
 * on the owner as {@code householdMemberCount}.
 *
 * <p>Runs after {@link AssignHousehold} (which assigns the shared {@code householdId} and back-fills
 * existing members) and before {@link SaveOwner}, so the count sees the existing members already in
 * the repository plus this owner, which is not yet persisted and so is added explicitly. A lone owner
 * with no {@code householdId} has a household of one. The mapper reads this count to award the
 * {@code GOLD} membership tier at three or more members.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            // A lone owner has no household id: the household is just this owner.
            owner.setHouseholdMemberCount(1);
            return;
        }
        int members = 1; // this owner, not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                members++;
            }
        }
        owner.setHouseholdMemberCount(members);
    }
}
