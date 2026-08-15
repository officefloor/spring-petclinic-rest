package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any existing owner.
 * Runs after {@link ValidateNewOwner} has normalized the request's email to its lower-cased form
 * (see {@link OwnerEmail}), and before {@link BuildOwner}, so a duplicate is a 409 rather than a
 * persisted record. Each stored owner's email is lower-cased the same way before comparing. A
 * request without an email supplies nothing to conflict with and is left alone.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = lower(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            if (email.equals(lower(owner.getEmail()))) {
                throw new DuplicateOwnerEmailException(request.getEmail());
            }
        }
    }

    private static String lower(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
