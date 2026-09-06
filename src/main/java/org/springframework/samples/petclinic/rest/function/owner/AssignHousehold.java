package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId} (see {@link Households}): the first 12 hex
 * characters of {@code SHA-256(normalizedLastName + '|' + postcode)}.
 *
 * <p>The id is derived from the last name and postcode alone, so every owner at the same last name
 * and postcode resolves to the same value automatically. It is assigned unconditionally. Duplicate
 * detection no longer depends on it (that is now the {@link OwnerIdentity#key(Owner) identityKey});
 * the household id is retained for household-size and membership derivations (see {@link Households}).
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(Households.id(owner));
    }
}
