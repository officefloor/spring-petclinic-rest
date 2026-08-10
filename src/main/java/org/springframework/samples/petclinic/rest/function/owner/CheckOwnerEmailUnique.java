package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailConflictException;

/**
 * Runs after {@link ValidateOwnerFields} (whose lower-cased email this step reads via {@code @Val})
 * and before {@link BuildOwner}: rejects the request with 409 when any existing owner already uses the
 * same email. Email is optional, so a request without one is left to pass. Comparison is on the
 * lower-cased value, so differently-cased duplicates are still caught.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerEmailConflictException {
        String email = normalize(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new OwnerEmailConflictException(
                        "An owner with email " + email + " already exists");
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
