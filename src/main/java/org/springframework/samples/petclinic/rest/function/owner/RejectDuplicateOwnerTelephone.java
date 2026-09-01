package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerTelephoneException;

/**
 * Rejects 409 when the built owner's normalized telephone is already used by any other owner.
 * Runs after {@link NormalizeOwnerTelephone}, so the telephone compared here is already digits-only;
 * existing owners' telephones are normalized the same way before comparing.
 */
public class RejectDuplicateOwnerTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerTelephoneException {
        String telephone = owner.getTelephone();
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && telephone.equals(normalize(other.getTelephone()))) {
                throw new DuplicateOwnerTelephoneException(telephone);
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? null : telephone.replaceAll("\\D", "");
    }
}
