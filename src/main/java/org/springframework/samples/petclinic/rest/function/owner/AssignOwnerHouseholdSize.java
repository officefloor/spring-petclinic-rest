package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the size of the owner's household — the number of owners sharing the same
 * {@code householdId}, counting this owner — as the transient {@code householdSize}.
 * Drives the 'GOLD' membership tier (a household of 3 or more members).
 *
 * <p>An owner with no household ({@code householdId} null or blank) has a household of
 * one. Runs on both the create pipeline (after Save, so the new owner is counted) and
 * the read pipeline, so the size — and therefore the tier — is identical whether returned
 * from the create response or a later GET, mirroring {@link AssignOwnerBulkSignupWarning}.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdSize(1);
            return;
        }
        long count = ownerRepository.findAll().stream()
                .filter(o -> householdId.equals(o.getHouseholdId()))
                .count();
        owner.setHouseholdSize((int) count);
    }
}
