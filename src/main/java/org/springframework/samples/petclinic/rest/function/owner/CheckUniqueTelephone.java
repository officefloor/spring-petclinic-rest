package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * Rejects a create whose normalized telephone is already used by any other owner,
 * before {@link SaveOwner} runs. Handled with 409 by {@code DuplicateTelephoneHandler}.
 */
public class CheckUniqueTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = owner.getTelephone();
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId())
                    && telephone.equals(normalize(other.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
