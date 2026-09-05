package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners in this owner's household
 * (owners sharing the same non-null {@code householdId}) counted <em>after</em> this create —
 * the existing members plus the owner being created. When the owner has no {@code householdId}
 * (not in a shared household), the size is 1.
 *
 * <p>Runs after {@link AssignHousehold} (so the {@code householdId} and any backfilled members
 * are settled) but before {@link SaveOwner} (so the owner being created is not itself in the
 * repository yet and is instead counted explicitly), mutating the owner in place so the
 * persisted value — and every later read — carries the household size as it stood after this
 * create.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int size = 1; // the owner being created (not yet saved)
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner no longer counts toward the household
                }
                if (householdId.equals(existing.getHouseholdId())) {
                    size++;
                }
            }
        }
        owner.setHouseholdSize(size);
    }
}
