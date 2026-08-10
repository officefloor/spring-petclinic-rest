package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other owner.
 * Email is optional: a request without an email is always allowed through. When present, the
 * request email has already been trimmed and lower-cased by {@link ValidateOwnerFields} (via
 * {@link OwnerEmail}); every existing owner's email is lower-cased the same way before comparison,
 * so a match on the lower-cased value is reported as a 409 (see {@link DuplicateEmailException}).
 *
 * <p>Runs after {@link CheckUniqueHousehold} (which republishes the validated request as a variable)
 * and before {@link BuildOwner}.
 */
public class CheckUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = normalize(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }
    }

    private static String normalize(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
