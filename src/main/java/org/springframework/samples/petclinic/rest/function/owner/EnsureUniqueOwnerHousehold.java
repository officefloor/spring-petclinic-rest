package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * On create, assigns the owner's deterministic {@link HouseholdId} (derived from last name and
 * postcode) and rejects the owner when an existing owner already shares that household. Setting
 * {@code sharesHousehold=true} only bypasses this rejection, declaring the owner a household
 * member; the id is assigned either way.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        String householdId = HouseholdId.of(owner);
        owner.setHouseholdId(householdId);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateHouseholdException(
                        "Another owner already shares this last name and postcode");
            }
        }
    }
}
