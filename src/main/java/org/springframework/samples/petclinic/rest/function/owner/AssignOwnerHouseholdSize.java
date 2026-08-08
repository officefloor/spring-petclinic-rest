package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners belong to the new owner's household once this create is
 * counted, i.e. the members already sharing its {@code householdId} plus the owner
 * being created. Runs after {@link AssignOwnerHousehold} (which sets the
 * {@code householdId}) and before {@link SaveOwner}, so the owner is not yet
 * persisted and therefore never counts itself. Mutates the {@link Owner} in place so
 * the snapshot is persisted and later read by the tier rule
 * ({@code membershipTier} is 'GOLD' when the household has three or more members).
 *
 * <p>When the owner did not join a household (no {@code householdId}) the size is 1:
 * the owner is its own single-member household.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1); // no shared household; a household of one
            return;
        }
        int count = 1; // the owner being created, not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never count the owner itself
            }
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
