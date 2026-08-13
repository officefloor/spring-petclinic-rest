package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's {@code membershipLevelCap}: a new owner's reported
 * {@code membershipLevel} cannot exceed one above the current maximum level among the
 * other members of their household. The cap is the highest {@link Owner#getMembershipLevel()}
 * over the already-saved, non-deleted owners that share this owner's {@code householdId},
 * plus one. When the household has no existing member, no cap applies (the field stays
 * {@code null}).
 *
 * <p>A <em>declared</em> household member ({@code sharesHousehold: true}) is exempt: it has
 * opted into the household and earns its level from genuine participation (the household-size
 * points), so its points-derived level stands uncapped. The cap therefore governs an owner
 * that merely shares a household key without declaring membership.
 *
 * <p>Runs after {@link AssignHousehold} (which assigns the shared {@code householdId}) and
 * the household/namesake counts, and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the members it reads over the repository exclude
 * the owner being created. It mutates the built {@link Owner} in place (see {@code @Val}
 * semantics), and the cap is persisted so later reads report the capped level. Existing
 * members contribute their own (already-capped) level, so the ceiling propagates through
 * the household as members are added.
 */
public class CapMembershipLevel {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member earns its level uncapped
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing == owner || existing.isDeleted()
                    || !householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            int level = existing.getMembershipLevel();
            if (maxLevel == null || level > maxLevel) {
                maxLevel = level;
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
