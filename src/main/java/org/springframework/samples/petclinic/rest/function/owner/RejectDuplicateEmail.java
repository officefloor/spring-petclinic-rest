package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose lower-cased email is already used by another owner.
 * Runs after {@link NormalizeOwnerEmail} (so the body carries the lower-cased email) and
 * before {@link BuildOwner}, comparing lower-cased emails against every existing owner. A
 * collision is rejected 409 via {@link DuplicateEmailException}. Email is optional; an
 * absent (null) email cannot collide, so the check is skipped.
 */
public class RejectDuplicateEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null
                    && normalized.equals(existingEmail.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(
                        "Email is already used by another owner");
            }
        }
    }
}
