package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the owner's {@code householdSize} at response time: the number of owners sharing this
 * owner's {@link Owner#getHouseholdId() householdId}, counting this owner itself. An owner with no
 * household id is a household of one. The size is exposed as a derived, response-time field on the
 * owner.
 *
 * <p>The current owner is counted explicitly (starting at one) rather than relying on it appearing
 * in {@link OwnerRepository#findAll()}, so the count is correct in the create pipeline — where the
 * owner has been stamped with its household id but not yet persisted — as well as on reads.
 *
 * <p>Wired into both the create pipeline (after {@link AssignOwnerHousehold} has stamped the
 * shared id) and the single-owner GET pipeline. Like {@code bulkSignupWarning} the value is
 * transient and set on the in-flight owner so the responder carries it; it is not persisted.
 */
public class DeriveOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // 1 for the owner itself plus its household members.
        owner.setHouseholdSize(1 + HouseholdMembers.of(owner, ownerRepository).size());
    }
}
