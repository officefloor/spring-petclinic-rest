package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * Rejects creating an owner that duplicates an existing one (same first name,
 * last name, address, city and telephone) with a 409 conflict.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (Objects.equals(existing.getFirstName(), owner.getFirstName())
                    && Objects.equals(existing.getLastName(), owner.getLastName())
                    && Objects.equals(existing.getAddress(), owner.getAddress())
                    && Objects.equals(existing.getCity(), owner.getCity())
                    && Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new OwnerAlreadyExistsException("Owner already exists");
            }
        }
    }
}
