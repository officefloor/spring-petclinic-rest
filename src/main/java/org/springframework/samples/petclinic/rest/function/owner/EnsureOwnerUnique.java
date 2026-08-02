package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * Rejects creating an owner whose telephone number is already used by any other
 * owner with a 409 conflict. Matching ignores letter case and surrounding or
 * repeated whitespace (see {@link DuplicateKey}).
 */
public class EnsureOwnerUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        String telephoneKey = DuplicateKey.of(owner.getTelephone());
        if (telephoneKey == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(DuplicateKey.of(existing.getTelephone()), telephoneKey)) {
                throw new OwnerAlreadyExistsException(
                        "Owner with telephone " + owner.getTelephone() + " already exists");
            }
        }
    }
}
