package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: stamps the new owner with its deterministic {@code householdId}.
 *
 * <p>The identifier is derived purely from the owner's last name and postcode (see
 * {@link OwnerIdentity#householdId(String, String)}), so every owner sharing a last name and
 * postcode carries the same value automatically &mdash; there is nothing to link and no existing
 * owner is touched. An owner with no postcode has no household and is left with a null
 * {@code householdId}. The {@code sharesHousehold} flag plays no part here: it only decides, back in
 * {@link RequireUniqueIdentity}, whether an owner joining an existing household is allowed rather
 * than rejected as a household duplicate.
 *
 * <p>Runs after {@link BuildOwner}, under the write transaction.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        owner.setHouseholdId(
            OwnerIdentity.householdId(region, owner.getLastName(), owner.getPostcode()));
    }
}
