package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a customer code formatted as
 * {@code <UPPERCASE_CITY>-<NNNN>}, where NNNN is one more than the number of owners
 * already in that city, zero-padded to four digits (e.g. {@code LONDON-0007}). Runs
 * before the owner is saved, so the count reflects only the owners that existed
 * beforehand. City matching is case-insensitive, consistent with the uppercased prefix.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long countInCity = ownerRepository.findAll().stream()
                .filter(existing -> existing.getCity() != null
                        && existing.getCity().equalsIgnoreCase(city))
                .count();
        String customerCode = String.format("%s-%04d", city.toUpperCase(), countInCity + 1);
        owner.setCustomerCode(customerCode);
    }
}
