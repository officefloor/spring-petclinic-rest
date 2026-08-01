package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * Rejects creation of an <code>Owner</code> that is identical to one already stored,
 * comparing first name, last name, address, city and telephone.
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isSameOwner(existing, owner)) {
                throw new OwnerAlreadyExistsException("An identical owner already exists");
            }
        }
    }

    private static boolean isSameOwner(Owner existing, Owner candidate) {
        return Objects.equals(existing.getFirstName(), candidate.getFirstName())
                && Objects.equals(existing.getLastName(), candidate.getLastName())
                && Objects.equals(existing.getAddress(), candidate.getAddress())
                && Objects.equals(existing.getCity(), candidate.getCity())
                && Objects.equals(existing.getTelephone(), candidate.getTelephone());
    }
}
