package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create-owner request whose normalized telephone is already used by any other
 * owner, with a 409 Conflict. Runs after {@link BuildOwner} has normalized the telephone.
 */
public class CheckOwnerTelephoneUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = TelephoneNormalizer.comparisonKey(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (telephone.equals(TelephoneNormalizer.comparisonKey(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(owner.getTelephone());
            }
        }
    }
}
