package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city3 = prefix3(owner.getCity());
        String last3 = prefix3(owner.getLastName());
        int sequence = countInCity(ownerRepository, owner.getCity()) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix3(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

    private static int countInCity(OwnerRepository ownerRepository, String city) {
        return (int) ownerRepository.findAll().stream()
            .filter(other -> city.equals(other.getCity()))
            .count();
    }
}
