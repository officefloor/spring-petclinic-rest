package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects a new owner whose normalized telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone} (so {@code owner} holds the digits-only
 * telephone) and before {@link SaveOwner}: if any existing owner has the same
 * normalized telephone, it throws a checked {@link DuplicateOwnerTelephoneException},
 * which the escalation handler turns into a 409 Conflict.
 */
public class RejectDuplicateOwnerTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = normalize(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (telephone.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(
                        "Telephone is already used by another owner");
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
