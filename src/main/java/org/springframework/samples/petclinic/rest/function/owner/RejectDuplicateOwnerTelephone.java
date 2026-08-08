package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects a new owner whose E.164 telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone} (so {@code owner} holds the E.164
 * telephone) and before {@link SaveOwner}: if any existing owner has the same
 * E.164 telephone, it throws a checked {@link DuplicateOwnerTelephoneException},
 * which the escalation handler turns into a 409 Conflict.
 */
public class RejectDuplicateOwnerTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = E164Telephone.toE164OrNull(owner.getTelephone());
        if (telephone == null) {
            return; // not a valid E.164 number; nothing to compare against
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (telephone.equals(E164Telephone.toE164OrNull(existing.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(
                        "Telephone is already used by another owner");
            }
        }
    }
}
