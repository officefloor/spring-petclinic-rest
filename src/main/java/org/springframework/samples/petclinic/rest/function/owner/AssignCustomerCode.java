package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long ownersInCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equals(existing.getCity()))
                .count();
        String code = String.format("%s-%04d", city.toUpperCase(Locale.ROOT), ownersInCity + 1);
        owner.setCustomerCode(code);
    }
}
