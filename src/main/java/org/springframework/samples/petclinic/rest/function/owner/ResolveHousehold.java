package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs after {@link ValidateNewOwner} and before {@link ValidateUniqueIdentity}. A read-only
 * step that derives the new owner's deterministic {@link Household.Id} from its last name and
 * postcode (see {@link Household}) and publishes it for the rest of the pipeline — a present id
 * when the request carries a postcode, {@link Household.Id#ABSENT} otherwise. It writes nothing
 * to the database.
 *
 * <p>The household id is derived purely from {@code (lastName, postcode)}, so every owner with a
 * postcode carries one and owners sharing a last name and postcode share it. It groups owners for
 * the household-size and membership-level rules — {@link BuildOwner} stamps the resolved id on the
 * new owner — but no longer gates duplicate detection, which is now the single owner identity key
 * ({@link OwnerIdentity}, checked by {@link ValidateUniqueIdentity}).
 */
public class ResolveHousehold {

    public void service(@Val OwnerFieldsDto request, Out<Household.Id> householdIdOut) {
        householdIdOut.set(Household.idFor(request));
    }
}
