package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by another owner with a
 * 409. Runs after {@code NormalizeOwnerEmail}, so the request email is already lower-cased; each
 * existing owner's email is lower-cased the same way before comparison. Email is optional, so an
 * absent or blank request email is left alone.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null
                    && email.equals(existingEmail.toLowerCase(Locale.ROOT))) {
                throw new DuplicateOwnerEmailException(request.getEmail());
            }
        }
    }
}
