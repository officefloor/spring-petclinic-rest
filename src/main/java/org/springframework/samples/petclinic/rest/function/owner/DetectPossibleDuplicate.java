package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.HouseholdId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a new owner as a soft duplicate when an existing owner shares its householdId
 * (last name and postcode) but has a different telephone — a likely household match that
 * is not a hard duplicate. Records {@code possibleDuplicateOf} with the matching owner's
 * id, or leaves the flag false when there is no such match. A declared household member
 * ({@code sharesHousehold} true) is never flagged. Runs before the new owner is saved, so
 * it inspects only pre-existing owners.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))
                    && !key(owner.getTelephone()).equals(key(existing.getTelephone()))) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
