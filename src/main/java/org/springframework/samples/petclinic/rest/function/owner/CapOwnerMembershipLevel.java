package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's membership level. A new owner's {@link MembershipLevel level} may not
 * exceed one above the CURRENT maximum level among the existing members of their household —
 * owners sharing the same {@link Household#id(Owner) householdId}, matched the same way as
 * {@link AssignOwnerHousehold}. The uncapped level is {@link MembershipLevel#of(Owner)}; the
 * capped result is stamped on the owner via {@link Owner#setMembershipLevel(Integer)} and later
 * returned as {@code membershipLevel}. With no existing household member no cap applies.
 *
 * <p>An existing member's level is its stored (already-capped) value when present, else the
 * derived {@link MembershipLevel#of(Owner)}. Runs after {@link AssignOwnerHousehold} and
 * {@link CountOwnerNamesakes} (so household id, size and namesake count are stamped) and before
 * {@link SaveOwner}.
 */
public class CapOwnerMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int level = MembershipLevel.of(owner);
        String householdId = owner.getHouseholdId();
        Integer maxExisting = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // an owner is not its own household member
            }
            if (householdId != null && householdId.equals(Household.id(existing))) {
                int existingLevel = existing.getMembershipLevel() != null
                        ? existing.getMembershipLevel()
                        : MembershipLevel.of(existing);
                if (maxExisting == null || existingLevel > maxExisting) {
                    maxExisting = existingLevel;
                }
            }
        }
        if (maxExisting != null && level > maxExisting + 1) {
            level = maxExisting + 1; // cap at one above the current household maximum
        }
        owner.setMembershipLevel(level);
    }
}
