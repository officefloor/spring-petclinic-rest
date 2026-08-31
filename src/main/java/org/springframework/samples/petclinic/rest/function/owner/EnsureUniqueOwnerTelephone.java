package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateTelephoneException;

/**
 * On create, rejects the owner when its normalized telephone already belongs to another
 * owner. Runs after {@code NormalizeOwnerTelephone}, so the built owner already holds its
 * 10-digit value; each stored telephone is normalized the same way before comparison.
 */
public class EnsureUniqueOwnerTelephone {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateTelephoneException {
        String telephone = digits(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && telephone.equals(digits(existing.getTelephone()))) {
                throw new DuplicateTelephoneException("Telephone is already used by another owner");
            }
        }
    }

    private static String digits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
