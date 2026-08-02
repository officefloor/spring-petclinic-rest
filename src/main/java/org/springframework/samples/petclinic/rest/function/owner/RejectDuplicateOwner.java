package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.ConflictException;

/**
 * Rejects creating an owner whose telephone number is already used by any other
 * owner, with a 409. Comparison ignores letter case and surrounding or repeated
 * whitespace (see {@link OwnerFieldNormalizer}).
 */
public class RejectDuplicateOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws ConflictException {
        String telephone = OwnerFieldNormalizer.normalize(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                && Objects.equals(telephone, OwnerFieldNormalizer.normalize(existing.getTelephone()))) {
                throw new ConflictException("An owner with the same telephone already exists");
            }
        }
    }
}
