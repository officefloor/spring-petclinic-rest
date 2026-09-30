package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's {@code householdId} when the request opts in with
 * {@code sharesHousehold} true. Housemates — see {@link OwnerHousehold} for the membership
 * rule — receive one stable shared
 * identifier: the first existing housemate's id is reused, otherwise a fresh one is minted
 * and back-filled onto every existing housemate so later joiners keep sharing it. Runs after
 * the owner is built and before it is saved; leaves {@code householdId} null when
 * {@code sharesHousehold} is not true.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }

        // One pass: collect the existing housemates and reuse any id they already share.
        List<Owner> housemates = new ArrayList<>();
        String householdId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerHousehold.isSameHousehold(owner.getLastName(), owner.getAddress(), existing)) {
                housemates.add(existing);
                if (householdId == null && existing.getHouseholdId() != null) {
                    householdId = existing.getHouseholdId();
                }
            }
        }
        if (householdId == null) {
            householdId = UUID.randomUUID().toString();
        }

        // Back-fill the shared id onto any housemate that lacks it, so the whole household
        // ends up on one identifier and later joiners reuse it.
        for (Owner housemate : housemates) {
            if (housemate.getHouseholdId() == null) {
                housemate.setHouseholdId(householdId);
                ownerRepository.save(housemate);
            }
        }

        owner.setHouseholdId(householdId);
    }
}
