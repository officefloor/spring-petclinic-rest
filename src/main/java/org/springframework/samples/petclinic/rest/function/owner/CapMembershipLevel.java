package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Caps the new owner's {@code membershipLevel} so it cannot exceed one above the current maximum
 * {@code membershipLevel} among the existing members of its household (owners sharing the same
 * {@link AssignHouseholdId#deriveHouseholdId householdId}). A joining member cannot leapfrog the
 * household: the level becomes {@code min(assignedLevel, maxExistingMemberLevel + 1)}.
 *
 * <p>The cap targets a <em>silent</em> joiner — one admitted into the household by
 * {@link RequireUniqueOwnerHousehold} on the strength of its own distinct email rather than by
 * declaring {@code sharesHousehold}. A member the caller explicitly declares with
 * {@code sharesHousehold} true is trusted and keeps its assigned level, so the tenure/points model
 * (e.g. the household-size bonus that lifts a declared third member to level 3) is preserved.
 *
 * <p>When there is no existing household member — the owner has no {@code householdId}, or is the
 * only member so far — no cap applies and the assigned level is left untouched.
 *
 * <p>Runs after {@link AssignMembershipLevel} (which assigns the uncapped level) and before
 * {@link SaveOwner}, so the capped value is what is persisted and returned. The new owner is not
 * yet saved, so the repository scan sees only the pre-existing members.
 */
public class CapMembershipLevel {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is trusted and not capped
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        Integer level = owner.getMembershipLevel();
        if (level == null) {
            return;
        }
        Integer maxExisting = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // never count the new owner against itself
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a household member
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            Integer existingLevel = existing.getMembershipLevel();
            if (existingLevel != null && (maxExisting == null || existingLevel > maxExisting)) {
                maxExisting = existingLevel;
            }
        }
        if (maxExisting == null) {
            return; // no existing household member -> no cap applies
        }
        int ceiling = maxExisting + 1;
        if (level > ceiling) {
            owner.setMembershipLevel(ceiling);
        }
    }
}
