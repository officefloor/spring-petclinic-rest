package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that runs after {@link BuildOwner} has produced the owner and
 * stamps its {@code householdId}. The id is derived deterministically from the owner's last name and
 * postcode (see {@link Households#householdId}), so every owner with the same last name and postcode
 * gets the same value automatically — the household needs no linking step and there is nothing to
 * back-fill.
 *
 * <p>Unlike the earlier behaviour, this is unconditional: {@code sharesHousehold} no longer creates
 * the household link (it only bypasses the household-duplicate block in {@link RejectDuplicateIdentity})
 * and even a lone owner carries the computed id. Downstream steps that key off the household —
 * {@link AssignHouseholdSize} and duplicate detection — read this same computed value.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.householdId(owner.getLastName(), owner.getPostcode()));
    }
}
