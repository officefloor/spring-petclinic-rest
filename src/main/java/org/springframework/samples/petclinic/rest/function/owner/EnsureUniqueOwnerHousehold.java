package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * On create, assigns the owner's deterministic {@link HouseholdId} (derived from last name and
 * postcode). Owners sharing a household are permitted to join it; a joiner's membership level is
 * capped against the existing members rather than the create being rejected.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdId.of(owner));
    }
}
