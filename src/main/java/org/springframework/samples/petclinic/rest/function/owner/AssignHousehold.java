package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Create-owner step: stamps the new owner with its {@link Household} identifier, computed
 * deterministically from its own last name and postcode. Every owner sharing a last name and
 * postcode therefore resolves to the same {@code householdId} without any owner-to-owner
 * linking. A household is keyed on the postcode, so an owner without one has no household and
 * is left unstamped.
 *
 * <p>Runs after {@link BuildOwner} and before {@link EnsureUniqueIdentity}, so the household
 * identifier is in place for the duplicate check that keys off it.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        owner.setHouseholdId(Household.id(owner.getLastName(), postcode));
    }
}
