package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other owner. Runs after
 * {@link NormalizeEmail} (so the request email is already lower-cased) and before the owner is built and
 * saved. Email is optional, so an absent or blank value is skipped. Each existing owner's email is
 * lower-cased the same way before comparison, so addresses stored in any case are matched by their
 * lower-cased value. A collision is rejected via {@link DuplicateEmailException}, which the global
 * handler turns into a 409.
 */
public class RejectDuplicateEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && email.equals(existingEmail.trim().toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }
    }
}
