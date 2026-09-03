package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

public class EnsureOwnerTelephoneUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        boolean taken = ownerRepository.findAll().stream()
                .anyMatch(other -> other.getTelephone() != null
                        && other.getTelephone().equals(owner.getTelephone()));
        if (taken) {
            throw new DuplicateTelephoneException("Telephone is already used by another owner");
        }
    }
}
