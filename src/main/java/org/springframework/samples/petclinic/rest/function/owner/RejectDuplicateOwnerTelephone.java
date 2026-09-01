package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects 409 when the built owner's telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone}, so the telephone compared here is already E.164;
 * existing owners' telephones are converted to E.164 the same way before comparing.
 */
public class RejectDuplicateOwnerTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = owner.getTelephone();
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && telephone.equals(E164Telephone.toE164(other.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
    }
}
