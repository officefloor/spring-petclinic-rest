package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's {@code membershipLevel} so it cannot exceed one above the current
 * maximum {@code membershipLevel} among the other members of its household. The household is
 * keyed on the deterministic {@code householdId} (normalized last name and postcode — see
 * {@link HouseholdKey}), so a member is any other owner that computes the same id.
 *
 * <p>Runs after {@link AssignMembershipLevel} has derived the raw level and before the owner is
 * saved, so the cap is applied against the already-persisted household members only. When the
 * owner has no existing household member (or none has a {@code membershipLevel}), no cap applies
 * and the derived level is kept as-is. Otherwise the stored level is reduced to at most
 * {@code max(existing membershipLevel) + 1}.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer level = owner.getMembershipLevel();
        if (level == null) {
            return;
        }
        HouseholdKey household = HouseholdKey.of(owner);
        OptionalInt maxExisting = ownerRepository.findAll().stream()
                .filter(existingOwner -> existingOwner.getId() == null
                        || !existingOwner.getId().equals(owner.getId()))
                .filter(existingOwner -> !Boolean.TRUE.equals(existingOwner.getDeleted()))
                .filter(household::matches)
                .map(Owner::getMembershipLevel)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max();
        if (maxExisting.isEmpty()) {
            return;
        }
        int ceiling = maxExisting.getAsInt() + 1;
        if (level > ceiling) {
            owner.setMembershipLevel(ceiling);
        }
    }
}
