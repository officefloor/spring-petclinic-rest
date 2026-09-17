package org.springframework.samples.petclinic.rest.function.owner;

import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Fixes the new owner's membership level at creation time, capped against their household: it may
 * exceed the highest effective level already held by an existing household member (one sharing this
 * owner's deterministic {@code householdId}, see {@link OwnerHouseholds}) by at most one. With no
 * existing household member no cap applies and the owner keeps its own derived level.
 *
 * <p>Runs after {@link AssignOwnerHouseholdSize} and {@link AssignOwnerNamesakeCount} — which set
 * the fields that feed {@link org.springframework.samples.petclinic.model.MembershipPoints} — and
 * before {@link SaveOwner}, so the recorded level reflects the household at creation time.
 */
public class AssignOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int level = MembershipLevel.of(owner);
        OptionalInt householdMax = householdMaxLevel(owner, ownerRepository);
        if (householdMax.isPresent()) {
            level = MembershipLevel.cappedToHousehold(level, householdMax.getAsInt());
        }
        owner.setMembershipLevel(level);
    }

    /** The highest effective membership level among the new owner's existing household members. */
    private static OptionalInt householdMaxLevel(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return OptionalInt.empty();
        }
        OptionalInt max = OptionalInt.empty();
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                int level = MembershipLevel.effective(existing);
                max = max.isPresent() ? OptionalInt.of(Math.max(max.getAsInt(), level)) : OptionalInt.of(level);
            }
        }
        return max;
    }
}
