package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the built owner with its deterministic {@link HouseholdNormalizer#id(String, String)
 * household id}, derived purely from the owner's last name and postcode. Because the id is a function
 * of those two fields alone, owners with the same last name and postcode share it automatically —
 * there is no scan of existing owners and no dependence on {@code sharesHousehold}. Runs after
 * {@link BuildOwner} and mutates the built {@link Owner} in place; the stamped id then drives the
 * duplicate block ({@link EnsureUniqueIdentity}), the household-size count ({@link CountHousehold})
 * and the possible-duplicate flag ({@link FlagPossibleDuplicate}).
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(HouseholdNormalizer.id(owner.getLastName(), owner.getPostcode()));
    }
}
