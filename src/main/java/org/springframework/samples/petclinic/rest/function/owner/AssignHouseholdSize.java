package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners that belong to the new owner's
 * household (share its {@code householdId}), including the owner being created.
 *
 * <p>Runs within the create transaction after {@link AssignHousehold} (which sets the
 * {@code householdId}) and before {@link SaveOwner}, so the count reflects the owners that already
 * exist plus this one. An owner with no household (no {@code householdId} — e.g. one created without
 * a postcode) is left with a {@code householdSize} of 1, since it belongs to no shared household.
 *
 * <p>The count is stored on the owner so {@code OwnerMapper} can derive the household membership
 * points purely from the owner's own fields, without going back to the database while mapping.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1; // include the owner being created, not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
