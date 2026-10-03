package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256
 * over the household key (normalized last name and postcode — see {@link HouseholdKey}). Every
 * owner is assigned one unconditionally, so owners with the same last name and postcode share the
 * same id automatically, with no opt-in and no back-filling of other owners.
 *
 * <p>The {@code sharesHousehold} create-time flag no longer plays any part here; it only tells
 * {@link RejectDuplicateIdentity} to let a declared household member through the duplicate block.
 * The stored id feeds {@link OwnerIdentity} and the owner DTO; the duplicate check and the
 * household-size count derive the household themselves from {@link HouseholdKey}, so they do not
 * depend on this stored value.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdKey.of(owner).householdId());
    }
}
