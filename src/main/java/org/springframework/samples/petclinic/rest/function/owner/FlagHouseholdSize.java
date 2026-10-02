package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Populates the response-only {@link Owner#getHouseholdSize() householdSize}: the number of owners
 * sharing this owner's {@link Owner#getHouseholdId() householdId}. Owners with no household id are
 * not part of a shared household, so they are assigned a size of {@code 1} (just themselves). The
 * membership tier derivation ({@link Owner#getMembershipTier()}) reads this to promote a household
 * of 3 or more members to {@code GOLD}. Mutates the loaded owner in place so the responder's mapper
 * carries the derived tier onto the DTO.
 *
 * <p>Note this counts by the stamped {@code householdId} — the stable identifier the create
 * pipeline writes onto joined members — not by {@link Household}'s name+address membership match.
 * The two keys are deliberately distinct (the spec phrases the GOLD rule in terms of shared
 * {@code householdId}), so they are not interchangeable.
 */
public class FlagHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
