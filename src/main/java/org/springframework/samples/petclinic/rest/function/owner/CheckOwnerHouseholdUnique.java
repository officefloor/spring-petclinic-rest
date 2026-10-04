package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time household guard for {@code POST /owners}: rejects a request whose last name and
 * address both match an existing owner's, with a 409 via {@link DuplicateHouseholdException}.
 *
 * <p>Last name and address are compared case-insensitively with collapsed whitespace, so values
 * that differ only in letter case or spacing count as the same household. A request may opt out
 * of the check by setting {@code sharesHousehold} true (several owners legitimately share one
 * household), in which case the duplicate is allowed.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerHousehold.matches(request, existing)) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
