package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners sharing this owner's
 * {@code householdId} once this create completes.
 *
 * <p>Runs after {@link AssignHouseholdId} — so the joining owner already carries its
 * {@code householdId} and every existing member has been back-filled with it — but before
 * {@link SaveOwner}, so the new owner is not yet persisted. The count is therefore the existing
 * members that share the identifier plus one for this owner.
 *
 * <p>When the owner is not part of a household ({@code householdId} is null), the size is one.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int size = 1; // this owner, not yet persisted
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    size++;
                }
            }
        }
        owner.setHouseholdSize(size);
    }
}
