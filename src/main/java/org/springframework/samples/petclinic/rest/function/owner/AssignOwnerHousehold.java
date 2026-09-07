package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Settles the household id for a new owner. When the request opted in with
 * {@code sharesHousehold} true and an existing owner is in the same household (see
 * {@link Household#sameHousehold(Owner, Owner)}), the two share a household: the stable
 * {@link Household#id(Owner, List) householdId} is stamped on the new owner and on every existing
 * member, and the household's size is recorded on all of them. Otherwise the owner is a household of
 * one.
 *
 * <p>This step no longer rejects duplicates — a shared household is not itself a conflict. All
 * duplicate detection is now the single {@link CheckOwnerIdentityUnique} step, which compares the
 * whole {@link OwnerIdentity#key(Owner) identityKey}. Because the household id set here is part of
 * that key, two members of the same household with different telephones have different keys and are
 * both allowed. Runs after {@link BuildOwner}.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold,
            OwnerRepository ownerRepository) {
        owner.setHouseholdSize(1); // a household of one until an existing member is found
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return; // did not opt in: never joins an existing household
        }
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save)
            }
            if (Household.sameHousehold(owner, existing)) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            return; // no existing owner in the same household
        }
        // Assign the same stable household identifier to all members, and record the household's
        // size (existing members plus this new owner) on every member so it reflects the
        // household as it stands after this create.
        String householdId = Household.id(owner, household);
        int householdSize = household.size() + 1;
        owner.setHouseholdId(householdId);
        owner.setHouseholdSize(householdSize);
        for (Owner member : household) {
            member.setHouseholdId(householdId);
            member.setHouseholdSize(householdSize);
            ownerRepository.save(member);
        }
    }
}
