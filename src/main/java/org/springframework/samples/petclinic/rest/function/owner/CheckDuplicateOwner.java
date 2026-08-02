package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner identical to one already stored — same first name, last
 * name, address, city and telephone — by throwing {@link DuplicateOwnerException} (409).
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (Objects.equals(existing.getFirstName(), owner.getFirstName())
                    && Objects.equals(existing.getLastName(), owner.getLastName())
                    && Objects.equals(existing.getAddress(), owner.getAddress())
                    && Objects.equals(existing.getCity(), owner.getCity())
                    && Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new DuplicateOwnerException("An identical owner already exists");
            }
        }
    }
}
