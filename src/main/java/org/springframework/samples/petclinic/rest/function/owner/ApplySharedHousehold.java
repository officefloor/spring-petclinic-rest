package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs after the {@link ValidateUniqueHousehold} gate and before {@link BuildOwner}, under
 * {@code govern: [ transaction ]}. When the gate resolved a shared {@link Household.Id} (the new
 * owner is joining an existing household), back-fills that identifier onto every existing member
 * that is missing it, so the whole household carries the same stable id. A no-op — publishing
 * nothing and touching no rows — when the new owner is not joining a shared household.
 */
public class ApplySharedHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Household.Id householdId,
            OwnerRepository ownerRepository) {
        if (householdId == null || !householdId.isPresent()) {
            return;
        }
        for (Owner member : Household.membersOf(request, ownerRepository)) {
            if (!householdId.value().equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId.value());
                ownerRepository.save(member);
            }
        }
    }
}
