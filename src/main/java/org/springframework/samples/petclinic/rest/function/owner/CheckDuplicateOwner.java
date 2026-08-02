package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects creating an owner whose telephone number is already used by any other owner —
 * by throwing {@link DuplicateOwnerException} (409).
 */
public class CheckDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String email = owner.getEmail();
        boolean hasEmail = (email != null) && !email.isBlank();
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(existing.getId(), owner.getId())) {
                continue;
            }
            if (Objects.equals(existing.getTelephone(), owner.getTelephone())) {
                throw new DuplicateOwnerException(
                        "An owner with the same telephone already exists");
            }
            if (hasEmail && Objects.equals(existing.getEmail(), email)) {
                throw new DuplicateOwnerException(
                        "An owner with the same email already exists");
            }
        }
    }
}
