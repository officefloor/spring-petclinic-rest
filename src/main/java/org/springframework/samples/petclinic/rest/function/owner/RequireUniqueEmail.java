package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other
 * owner, before {@link BuildOwner} runs. Email is optional; an absent email is skipped.
 * The request email is already lower-cased by {@link NormaliseEmail}; existing emails are
 * lower-cased here for the comparison so the match is case-insensitive.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && email.equals(other.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
