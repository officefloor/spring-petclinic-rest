package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.MembershipLevels;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the owner's membership level as the transient {@code membershipLevel}, capping it so
 * it never exceeds one above the current maximum level among the <em>other</em> members of its
 * household (owners sharing the same {@code householdId}, this owner excluded). With no other
 * household member no cap applies and the uncapped level stands.
 *
 * <p>Runs after {@link AssignOwnerHouseholdSize} — the level depends on the household size — on
 * both the create pipeline (after Save, so the new owner has an id and is excluded by it) and
 * the read pipeline, so the capped level is identical whether returned from the create response
 * or a later GET, mirroring {@link AssignOwnerHouseholdSize} and {@link AssignOwnerPossibleDuplicate}.
 */
public class AssignOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setMembershipLevel(MembershipLevels.of(owner));
            return;
        }
        Integer householdSize = owner.getHouseholdSize();
        List<Owner> others = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null
                    || (owner.getId() != null && existing.getId().equals(owner.getId()))) {
                continue;
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            // All members of a household share the same size; align it so each member's level
            // is computed on the same basis a GET would report for that member.
            existing.setHouseholdSize(householdSize);
            others.add(existing);
        }
        owner.setMembershipLevel(MembershipLevels.capped(owner, others));
    }
}
