package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs on {@code POST /api/owners} after {@link BuildOwner}, when the request opts in with
 * {@code sharesHousehold} true. When another owner already shares the household — same last name
 * and same address, compared case-insensitively with collapsed whitespace — every member of that
 * household is given one stable shared {@code householdId}: an existing member's id is reused if
 * present, otherwise a new id is derived deterministically from the normalized last name and
 * address (so independent joiners compute the same value). The new owner and any member lacking an
 * id are assigned it, and the existing members are saved so the whole household shares the
 * identifier. Without a matching owner, or when {@code sharesHousehold} is not set, no household is
 * formed and no id is assigned. The membership rule is shared with {@link CheckUniqueOwnerIdentity}
 * through {@link OwnerHousehold}, so the householdId used in the identity key matches the one stored.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> household = OwnerHousehold.matchingMembers(request, ownerRepository);
        if (household.isEmpty()) {
            return;
        }
        String householdId = OwnerHousehold.existingId(household);
        if (householdId == null) {
            householdId = OwnerHousehold.deriveId(request);
        }
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }
}
