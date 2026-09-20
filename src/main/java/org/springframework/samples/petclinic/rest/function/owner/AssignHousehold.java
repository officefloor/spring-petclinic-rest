package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * When a create request opts into a shared household ({@code sharesHousehold} true) and an
 * existing owner already lives at the same address under the same last name, joins the new
 * owner to that household: every owner in it — the existing members and the new one — is
 * stamped with the same stable {@link Households#id(String, String) household id}. Owners
 * that are not part of a shared household keep a null id.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = Households.id(request.getLastName(), request.getAddress());
        boolean joined = false;
        for (Owner existing : ownerRepository.findAll()) {
            if (Households.matches(existing, request.getLastName(), request.getAddress())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
                joined = true;
            }
        }
        if (joined) {
            owner.setHouseholdId(householdId);
        }
    }
}
