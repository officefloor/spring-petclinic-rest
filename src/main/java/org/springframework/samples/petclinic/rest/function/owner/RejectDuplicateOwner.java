package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.ConflictException;

/**
 * Rejects creating an owner when another owner already has the same last name
 * and the same telephone number, with a 409.
 */
public class RejectDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws ConflictException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (Objects.equals(owner.getLastName(), existing.getLastName())
                && Objects.equals(owner.getTelephone(), existing.getTelephone())) {
                throw new ConflictException("An owner with the same last name and telephone already exists");
            }
        }
    }
}
