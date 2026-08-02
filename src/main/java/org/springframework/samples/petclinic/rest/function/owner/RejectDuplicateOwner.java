package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.ConflictException;

/**
 * Rejects creating an owner that is identical to an already-existing one
 * (same first name, last name, address, city and telephone) with a 409.
 */
public class RejectDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws ConflictException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isSameOwner(owner, existing)) {
                throw new ConflictException("An identical owner already exists");
            }
        }
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return Objects.equals(a.getFirstName(), b.getFirstName())
            && Objects.equals(a.getLastName(), b.getLastName())
            && Objects.equals(a.getAddress(), b.getAddress())
            && Objects.equals(a.getCity(), b.getCity())
            && Objects.equals(a.getTelephone(), b.getTelephone());
    }
}
