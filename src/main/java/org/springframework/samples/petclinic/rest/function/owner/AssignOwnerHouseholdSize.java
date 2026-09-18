package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in {@code POST /api/owners} after the owner's household id has been assigned and before
 * it is saved, within the create transaction. Records on the built owner how many members its
 * household holds once this create completes: the owners already sharing the same
 * {@link Owner#getHouseholdId() household id} plus the new owner itself. Because the new owner
 * has not yet been persisted, {@link OwnerRepository#findAll()} returns only the existing
 * members, so the {@code + 1} accounts for the owner being created. An owner not joining a
 * household (no household id) counts as a household of one.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        long members = 1; // the owner being created, not yet persisted
        if (householdId != null) {
            members += ownerRepository.findAll().stream()
                    .filter(existing -> householdId.equals(existing.getHouseholdId()))
                    .count();
        }
        owner.setHouseholdSize((int) members);
    }
}
