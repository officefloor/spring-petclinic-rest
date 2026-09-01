package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a new owner whose normalized telephone (set by {@link BuildOwner}) already belongs to
 * another owner. Runs after Build and before Save so the conflict is a 409, not a persisted duplicate.
 */
public class EnsureUniqueTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DuplicateTelephoneException {
        String telephone = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (telephone.equals(existing.getTelephone())) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
