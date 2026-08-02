package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a customer code to a newly built owner, formatted
 * '<UPPERCASE_CITY>-<NNNN>' where NNNN is one more than the number of owners
 * already in that city, zero-padded to four digits (e.g. 'LONDON-0007').
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long existingInCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        String code = String.format("%s-%04d", city.toUpperCase(Locale.ROOT), existingInCity + 1);
        owner.setCustomerCode(code);
    }
}
