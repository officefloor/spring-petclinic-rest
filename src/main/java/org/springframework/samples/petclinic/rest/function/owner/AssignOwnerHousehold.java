package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Settles the household id for a new owner. The {@link Household#id(Owner) householdId} is now
 * DETERMINISTIC — the first 12 hex characters of SHA-256 over the normalized last name and
 * postcode — so it is always stamped on the owner regardless of {@code sharesHousehold}: owners
 * that share a last name and postcode automatically carry the identical value. The household's
 * size (this owner plus every existing member sharing the same householdId) is recorded on the
 * owner. Runs after {@link BuildOwner}.
 *
 * <p>This step no longer rejects duplicates and no longer needs {@code sharesHousehold} to form the
 * link: the link is implied by the shared householdId. Rejecting a household duplicate is
 * {@link CheckOwnerIdentityUnique}, which now keys off this householdId; {@code sharesHousehold}
 * only bypasses that rejection.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = Household.id(owner);
        owner.setHouseholdId(householdId);
        int householdSize = 1; // this owner
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save)
            }
            if (householdId.equals(Household.id(existing))) {
                householdSize++;
            }
        }
        owner.setHouseholdSize(householdSize);
    }
}
