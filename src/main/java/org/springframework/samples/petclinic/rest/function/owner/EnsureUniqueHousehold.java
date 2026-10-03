package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * The single duplicate check on create, keyed on the household. A household is
 * {@code (lastName, postcode)} (see {@link Households}), so two owners that share a last
 * name and postcode are the same household and the second is rejected as a household
 * duplicate via {@link DuplicateHouseholdException} (409).
 *
 * <p>The {@code sharesHousehold} request flag bypasses this block entirely: a request that
 * opts in is a declared household member and is always created (it is also not flagged as a
 * possible duplicate — see {@link FlagPossibleDuplicate}). An owner with no postcode belongs
 * to no household and so can never collide here.
 *
 * <p>The household id is derived from the request's last name and postcode and compared
 * against the id each existing owner computes from its own last name and postcode, so the
 * check and {@link AssignHousehold}'s stamp cannot drift apart.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = Households.householdId(request.getLastName(), request.getPostcode());
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null) {
                continue;
            }
            if (householdId.equals(Households.householdId(existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateHouseholdException(householdId);
            }
        }
    }
}
