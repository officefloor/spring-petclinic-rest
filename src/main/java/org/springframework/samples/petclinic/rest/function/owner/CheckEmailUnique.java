package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects creating an owner whose lower-cased email is already used by any other owner,
 * so the create endpoint responds 409 instead of storing a duplicate. Runs after
 * {@link ValidateOwner} (which lower-cases the email) and before {@link BuildOwner},
 * comparing against every existing owner's email lower-cased. The email is optional, so
 * an absent or blank email is left alone.
 */
public class CheckEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = normalize(request.getEmail());
        if (email.isEmpty()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }
    }

    /** Lower-cases and trims so comparison ignores case; null/blank becomes empty. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
