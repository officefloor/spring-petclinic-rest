package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create request opts in with {@code sharesHousehold=true}, assigns the new owner and every
 * existing same-lastName owner at the same address a stable shared {@code householdId}. The id is
 * derived deterministically from the normalised last name and address, so all members of one
 * household compute the same value. A request that does not opt in is left untouched.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        String householdId = String.format("HH-%08X", (lastName + "|" + address).hashCode());
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
