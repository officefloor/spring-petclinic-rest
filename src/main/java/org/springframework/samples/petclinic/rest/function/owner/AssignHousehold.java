package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId} (see {@link Households}): the first 12 hex
 * characters of {@code SHA-256(normalizedLastName + '|' + postcode)}.
 *
 * <p>The id is derived from the last name and postcode alone, so every owner at the same last name
 * and postcode resolves to the same value automatically. It is assigned unconditionally — the
 * request's {@code sharesHousehold} flag no longer creates the link, it only bypasses the household
 * duplicate block in {@link EnsureUniqueIdentity}.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.id(owner));
    }
}
