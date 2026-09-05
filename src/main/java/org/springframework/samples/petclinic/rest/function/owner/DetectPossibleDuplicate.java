package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft duplicate before the new owner is saved. A hard duplicate (matching
 * telephone) has already been rejected by {@link RequireUniqueIdentity}; this records
 * the id of an existing owner in the same {@link HouseholdId} (lastName and postcode) that
 * has a different telephone, so the response can surface {@code possibleDuplicate}/
 * {@code possibleDuplicateOf}. A declared household member ({@code sharesHousehold}) is not
 * a suspected duplicate, and there is no match when there is no postcode. */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String householdId = HouseholdId.of(owner.getLastName(), postcode);
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))
                    && !equalsTelephone(owner, existing)) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static boolean equalsTelephone(Owner owner, Owner existing) {
        String telephone = owner.getTelephone();
        return telephone != null && telephone.equals(existing.getTelephone());
    }
}
