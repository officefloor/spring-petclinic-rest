package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code householdSize}: the number of owners that share this owner's
 * {@code householdId} once this owner is created, counting the new owner itself. When the owner has
 * no household (no {@code householdId}, i.e. the request did not opt in with {@code sharesHousehold}),
 * the size is 1.
 *
 * <p>The value is a snapshot taken while the new row is still unsaved, so it captures the household's
 * membership immediately after this create. A household of 3 or more members earns the {@code GOLD}
 * membership tier at read time.
 *
 * <p>Runs after {@link AssignHouseholdId} (so the {@code householdId} is set and any pre-existing
 * members are back-filled) and before {@link SaveOwner}, mutating the not-yet-persisted owner in
 * place.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1; // the owner being created
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
