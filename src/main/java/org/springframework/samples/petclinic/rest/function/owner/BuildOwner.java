package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // Default an unsupplied registration date to the server's current date.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        owner.setCustomerCode(nextCustomerCode(owner.getLastName(), ownerRepository));
        built.set(owner);
    }

    /**
     * Build the '<LAST3>-<NNNN>' customer code: LAST3 is the upper-cased first three
     * letters of the last name and NNNN is a global 4-digit zero-padded sequence equal
     * to one more than the current number of owners (e.g. 'SMI-0007').
     */
    private static String nextCustomerCode(String lastName, OwnerRepository ownerRepository) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = ownerRepository.findAll().size() + 1;
        return String.format("%s-%04d", last3, sequence);
    }
}
