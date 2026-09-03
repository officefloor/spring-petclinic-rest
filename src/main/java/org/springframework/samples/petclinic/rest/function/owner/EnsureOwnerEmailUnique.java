package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

public class EnsureOwnerEmailUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        boolean taken = owner.getEmail() != null && ownerRepository.findAll().stream()
                .anyMatch(other -> owner.getEmail().equals(other.getEmail()));
        if (taken) {
            throw new DuplicateEmailException("Email is already used by another owner");
        }
    }
}
