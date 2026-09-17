package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Create-owner step: rejects the request with a 409 when its email, compared lower-cased,
 * is already used by any existing owner. Email is optional, so a missing or blank value is
 * left unchecked. Runs after {@link NormalizeOwnerEmail} (so the request already holds the
 * lower-cased value) and before {@link BuildOwner}. Mirrors {@link EnsureUniqueTelephone}.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            if (email.equals(lowerCased(owner.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }

    /** An existing email lower-cased, or {@code null} when absent (never matches). */
    private static String lowerCased(String stored) {
        return stored == null ? null : stored.toLowerCase(Locale.ROOT);
    }
}
