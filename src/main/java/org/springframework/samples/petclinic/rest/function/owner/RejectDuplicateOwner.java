package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner identical to an existing one (same first name, last name, address, city
 * and telephone), responding 409 via {@link DuplicateOwnerException}. Runs after {@code build} and
 * before {@code save}.
 */
public class RejectDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateOwnerException {
        for (Owner existing : ownerRepository.findByLastName(owner.getLastName())) {
            if (isSameOwner(existing, owner)) {
                throw new DuplicateOwnerException("An identical owner already exists");
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
