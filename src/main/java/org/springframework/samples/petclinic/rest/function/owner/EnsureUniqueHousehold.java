package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The duplicate block: rejects a create request whose {@link Households#id(OwnerFieldsDto)
 * household id} — its last name and postcode — matches an existing owner's, because owners in
 * the same household are otherwise indistinguishable at registration. A request may opt out by
 * setting {@code sharesHousehold}, declaring it a genuine second member of that household; that
 * only bypasses this block, since the household link itself is assigned deterministically
 * later (see {@link AssignHousehold}).
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws HouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = Households.id(request);
        for (Owner existing : ownerRepository.findAllActive()) {
            if (householdId.equals(Households.id(existing))) {
                throw new HouseholdDuplicateException(householdId);
            }
        }
    }
}
