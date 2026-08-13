package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs on {@code POST /api/owners} after {@link BuildOwner}, and assigns the new owner its
 * deterministic {@code householdId}: the first 12 hex characters of SHA-256 over
 * {@code <normalizedLastName>|<postcode>} (see {@link OwnerHousehold}). Every owner gets one, so
 * owners with the same last name and postcode share it automatically — no {@code sharesHousehold}
 * opt-in and no lookup of existing members is needed. The same computation is used by
 * {@link CheckUniqueOwnerIdentity}, so the householdId used in the identity key and the household
 * duplicate check matches the one stored and returned.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner) {
        owner.setHouseholdId(OwnerHousehold.householdId(request));
    }
}
