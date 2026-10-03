package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects creating an owner whose household — see {@link HouseholdKey} — is already used by
 * another (existing) owner. A match on a different owner escalates as a 409 via
 * {@link DuplicateHouseholdException}.
 *
 * <p>The check is skipped when the request set {@code sharesHousehold} true (published by
 * {@link BuildOwner}): such owners are allowed to share a household.
 */
public class RejectDuplicateHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        HouseholdKey household = HouseholdKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (household.matches(existing)) {
                throw new DuplicateHouseholdException(
                        "Another owner already shares this household (same last name and address)");
            }
        }
    }
}
