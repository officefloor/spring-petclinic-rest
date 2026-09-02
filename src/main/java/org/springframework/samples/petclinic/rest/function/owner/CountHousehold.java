package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners belong to this owner's household (same last name and
 * address, normalized as in the duplicate-household guard) after this create, i.e.
 * the existing members plus the new owner. Drives the 'GOLD' membership tier.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        long existing = ownerRepository.findAll().stream()
                .filter(other -> lastName.equals(normalize(other.getLastName()))
                        && address.equals(AddressNormalizer.normalize(other.getAddress())))
                .count();
        owner.setHouseholdCount((int) existing + 1);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
