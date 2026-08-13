package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other
 * owner, responding 409 via {@link DuplicateEmailException}.
 *
 * <p>Email is optional: a request without an email is never a duplicate and passes
 * through. Runs after {@link ValidateOwnerFields} (which has already lower-cased the
 * request's email) and before {@link BuildOwner} saves anything. Each stored owner's
 * email is lower-cased the same way before comparison, so addresses differing only in
 * case still collide.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = normalize(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
