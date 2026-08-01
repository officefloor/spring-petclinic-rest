package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creation of an owner that is identical to one already stored (same first
 * name, last name, address, city and telephone).
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isIdentical(owner, existing)) {
                throw new DuplicateOwnerException("An identical owner already exists");
            }
        }
    }

    private static boolean isIdentical(Owner a, Owner b) {
        return Objects.equals(a.getFirstName(), b.getFirstName())
                && Objects.equals(a.getLastName(), b.getLastName())
                && Objects.equals(a.getAddress(), b.getAddress())
                && Objects.equals(a.getCity(), b.getCity())
                && Objects.equals(a.getTelephone(), b.getTelephone());
    }
}
