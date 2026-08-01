package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the household-sharing flag to a newly created owner: {@code true} if another
 * owner already had the same address and city at the moment of creation, otherwise
 * {@code false}. The owner being created has not yet been saved, so it never matches
 * itself. Address and city are matched case- and whitespace-insensitively, consistent
 * with the other owner uniqueness rules.
 */
public class AssignSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        boolean shares = false;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (DuplicateMatching.matches(existing.getAddress(), owner.getAddress())
                    && DuplicateMatching.matches(existing.getCity(), owner.getCity())) {
                shares = true;
                break;
            }
        }
        owner.setSharesHousehold(shares);
    }
}
