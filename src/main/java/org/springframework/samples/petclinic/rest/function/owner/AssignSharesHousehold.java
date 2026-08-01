package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether another owner already shares this owner's household (same address
 * and city) at the moment of creation. Runs before the owner is saved, so
 * {@link OwnerRepository#findAll()} yields only the existing owners; creation is
 * still allowed either way.
 */
public class AssignSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = owner.getAddress();
        String city = owner.getCity();
        boolean shares = ownerRepository.findAll().stream()
                .anyMatch(existing -> address != null && city != null
                        && address.equals(existing.getAddress())
                        && city.equals(existing.getCity()));
        owner.setSharesHousehold(shares);
    }
}
