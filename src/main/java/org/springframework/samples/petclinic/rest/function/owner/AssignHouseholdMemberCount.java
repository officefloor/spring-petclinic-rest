package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdMemberCount}: the number of owners that share the
 * owner's {@code householdId} once this create completes — the already-saved members of
 * the household plus the owner being created. An owner with no {@code householdId} (one
 * that did not opt in to a shared household) counts as a household of one.
 *
 * <p>Runs after {@link AssignHousehold} (which assigns the shared {@code householdId} and
 * back-fills existing members) and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the count it reads over the repository excludes
 * the owner being created; adding one for that owner yields the size of the household
 * after this create. It mutates the built {@link Owner} in place (see {@code @Val}
 * semantics), and the value is persisted with the new owner and used to derive the
 * {@code GOLD} membership tier.
 */
public class AssignHouseholdMemberCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1;
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    count++;
                }
            }
        }
        owner.setHouseholdMemberCount(count);
    }
}
