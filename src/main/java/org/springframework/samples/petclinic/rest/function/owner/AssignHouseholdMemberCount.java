package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdMemberCount}: the number of owners in this owner's household
 * (owners sharing the same {@code householdId}) counting this new owner, i.e. the size of the
 * household immediately after this create.
 *
 * <p>Runs after {@link AssignHouseholdId} has set the shared {@code householdId} and before
 * {@link SaveOwner}, within the same write transaction. It counts the owners persisted so far that
 * share this owner's householdId and adds one for this new, not-yet-saved owner, so the value
 * reflects the household size after this create.
 */
public class AssignHouseholdMemberCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1; // this new owner, not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId != null && householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdMemberCount(count);
    }
}
