package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Collection;
import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code membershipPoints} and the
 * {@code membershipLevel} derived from them. The scoring rules and the points-to-level
 * mapping live in {@link MembershipPoints}; this step gathers the factors and stores both
 * values. The derived level is then capped to at most one above the highest level already in
 * the owner's {@link Household} (uncapped when the household has no existing member). It runs
 * after {@link CountNamesakes} so the namesake count is known,
 * {@link AssignHousehold} so the household is stamped, and
 * {@link ResolveOwnerRegistrationDate} so the registration date is resolved, and before
 * {@link SaveOwner} persists the owner and {@link AuditOwnerCreated} records the level. The
 * values are stored with the row and returned unchanged on later reads.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Collection<Owner> existingOwners = ownerRepository.findAll();
        // The new owner is not yet persisted, so it is counted here rather than by the repository.
        long householdSize = Household.size(owner.getHouseholdId(), existingOwners) + 1;
        long tenureFiscalYears = Tenure.fiscalYears(owner.getRegistrationDate(), LocalDate.now());
        int points = MembershipPoints.score(owner.getEmail(), owner.getNamesakeCount(), householdSize, tenureFiscalYears);
        owner.setMembershipPoints(points);

        // A new owner may sit at most one level above the highest already in its household;
        // with no existing household member the level is left uncapped.
        int level = MembershipPoints.level(points);
        OptionalInt householdMax = Household.maxMembershipLevel(owner.getHouseholdId(), existingOwners);
        if (householdMax.isPresent()) {
            level = MembershipPoints.capToHousehold(level, householdMax.getAsInt());
        }
        owner.setMembershipLevel(level);
    }
}
