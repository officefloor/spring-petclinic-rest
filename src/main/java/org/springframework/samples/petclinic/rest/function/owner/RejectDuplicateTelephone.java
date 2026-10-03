package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects creating an owner whose normalized telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone} (so the owner's telephone is already the
 * normalized 10-digit value) and before {@link SaveOwner}. Each stored telephone is
 * normalized through {@link OwnerTelephones} before comparison, so a stored value in any
 * format still collides. A match on a different owner escalates as a 409 via
 * {@link DuplicateTelephoneException}.
 */
public class RejectDuplicateTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        // The incoming owner's telephone is already normalized by NormalizeOwnerTelephone;
        // only the stored values (which may predate the pipeline) need normalizing to compare.
        String telephone = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (telephone.equals(OwnerTelephones.normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone is already used by another owner");
            }
        }
    }
}
