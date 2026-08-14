package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in the create-owner pipeline after {@link AssignHousehold} (which stamps the shared
 * {@code householdId}) and before {@link SaveOwner}. Records how many owners share this owner's
 * household as of this create.
 *
 * <p>The owner being created is not yet persisted, so its own membership is counted with the seed
 * {@code 1} and every already-stored owner carrying the same non-null {@code householdId} adds one.
 * An owner with no household ({@code householdId == null}) is a household of one and never groups with
 * other household-less owners.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1); // unique household — just this owner
            return;
        }
        int members = 1; // this owner, not yet saved
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                members++;
            }
        }
        owner.setHouseholdSize(members);
    }
}
