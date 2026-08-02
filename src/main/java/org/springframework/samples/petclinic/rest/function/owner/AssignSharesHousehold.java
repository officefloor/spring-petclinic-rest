package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether the newly built owner shares a household with an existing one:
 * {@code true} when another owner already has the same address and city at the
 * moment this owner is created, {@code false} otherwise. The new owner is not yet
 * saved, so every matching owner is a distinct, pre-existing one. Runs after the
 * city has been normalised so it compares against each city's canonical spelling.
 */
public class AssignSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = owner.getAddress();
        String city = owner.getCity();
        boolean shares = ownerRepository.findAll().stream()
                .anyMatch(existing -> existing.getAddress() != null
                        && existing.getAddress().equals(address)
                        && existing.getCity() != null
                        && existing.getCity().equals(city));
        owner.setSharesHousehold(shares);
    }
}
