package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Runs after {@link ValidateUniqueTelephone} and before {@link BuildOwner}. A read-only
 * validation gate: it detects whether the new owner would join an existing household (see
 * {@link Household}) and decides allow or reject, writing nothing to the database.
 *
 * <p>When another owner already shares the household and the request did not opt in with
 * {@code sharesHousehold = true}, rejects the create with a 409. Otherwise it publishes the
 * {@link Household.Id} for the rest of the pipeline — the shared identifier when joining a
 * household, {@link Household.Id#ABSENT} when not — leaving {@link ApplySharedHousehold} to
 * back-fill existing members and {@link BuildOwner} to stamp the new owner.
 */
public class ValidateUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            Out<Household.Id> householdIdOut) throws DuplicateHouseholdException {
        List<Owner> members = Household.membersOf(request, ownerRepository);
        if (members.isEmpty()) {
            // Not joining any household: nothing to share, nothing to reject.
            householdIdOut.set(Household.Id.ABSENT);
            return;
        }
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            throw new DuplicateHouseholdException("An owner named " + request.getLastName()
                    + " already exists at " + request.getAddress());
        }
        householdIdOut.set(Household.idFor(request));
    }
}
