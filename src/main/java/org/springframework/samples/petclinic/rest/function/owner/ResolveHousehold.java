package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs after {@link ValidateNewOwner} and before {@link ValidateUniqueIdentity}. A read-only
 * step that resolves the new owner's household (see {@link Household}) and publishes the
 * {@link Household.Id} for the rest of the pipeline — the shared identifier when the owner joins
 * an existing household (same last name at the same address), {@link Household.Id#ABSENT}
 * otherwise. It writes nothing to the database.
 *
 * <p>The household no longer rejects the create on its own: duplicate detection is now the single
 * {@link OwnerIdentity identity key} gate in {@link ValidateUniqueIdentity}. Because the household
 * id is part of that key, two members of the same household with different telephones derive
 * different keys and are both allowed. {@link ApplySharedHousehold} later back-fills existing
 * members and {@link BuildOwner} stamps the new owner with the resolved id.
 */
public class ResolveHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            Out<Household.Id> householdIdOut) {
        List<Owner> members = Household.membersOf(request, ownerRepository);
        if (members.isEmpty()) {
            // Not joining any household: no shared id to publish.
            householdIdOut.set(Household.Id.ABSENT);
            return;
        }
        householdIdOut.set(Household.idFor(request));
    }
}
