package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link BuildOwner}. Stamps the deterministic household
 * identifier computed by {@link DeriveHouseholdId} onto the owner being created so it is stored and
 * returned. Every owner with a postcode gets one (owners sharing lastName and postcode share the
 * value); an owner with no postcode has no shared household and the identifier is {@code null}.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val HouseholdId householdId) {
        if (householdId != null && householdId.value() != null) {
            owner.setHouseholdId(householdId.value());
        }
    }
}
