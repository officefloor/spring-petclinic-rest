package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects creating an owner whose E.164 telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone} (so the owner's telephone is already the E.164
 * value) and before {@link SaveOwner}. Each stored telephone is converted to E.164 through
 * {@link OwnerTelephones} before comparison, so a stored value in any format still collides.
 * A match on a different owner escalates as a 409 via {@link DuplicateTelephoneException}.
 */
public class RejectDuplicateTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        // The incoming owner's telephone is already E.164 (from NormalizeOwnerTelephone);
        // only the stored values (which may predate the pipeline) need converting to compare.
        String telephone = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            // A stored value that cannot form valid E.164 yields null, which never matches.
            if (telephone.equals(OwnerTelephones.toE164(existing.getTelephone()))) {
                throw new DuplicateTelephoneException(
                        "Telephone is already used by another owner");
            }
        }
    }
}
