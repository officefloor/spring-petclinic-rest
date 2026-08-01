package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner a customer code formatted '<UPPERCASE_CITY>-<NNNN>' where NNNN
 * is one more than the number of owners already in that city, zero-padded to four
 * digits (e.g. 'LONDON-0007'). Runs before the owner is saved, so the current
 * per-city count excludes this owner.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long ownersInCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equals(existing.getCity()))
                .count();
        String code = String.format("%s-%04d", city == null ? "" : city.toUpperCase(), ownersInCity + 1);
        owner.setCustomerCode(code);
    }
}
