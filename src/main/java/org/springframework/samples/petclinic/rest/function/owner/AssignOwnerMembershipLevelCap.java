package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the built owner's membership level to at most one above the highest membership level
 * among the existing members of its household — the owners with the same last name and
 * postcode (see {@link OwnerHouseholds}). With no existing household member no cap applies.
 *
 * <p>Runs after the owner is built but before it is saved, so the household reflects the owners
 * that existed before this create. Each member's current level is read through
 * {@link OwnerMapper#membershipLevel(Owner)} so the ceiling is one above their effective level.
 * The ceiling is stored on the built {@link Owner} (mutated in place) and later applied by the
 * same mapper method when the owner's level is derived for a response.
 */
public class AssignOwnerMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        Integer cap = null;
        for (Owner member : OwnerHouseholds.members(Owners.active(ownerRepository.findAll()),
                owner.getLastName(), owner.getPostcode())) {
            int ceiling = ownerMapper.membershipLevel(member) + 1;
            if (cap == null || ceiling > cap) {
                cap = ceiling;
            }
        }
        owner.setMembershipLevelCap(cap);
    }
}
