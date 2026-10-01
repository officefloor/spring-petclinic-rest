package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDuplicateException;

/**
 * Runs after {@link ValidateOwner} (which has lower-cased the email and republished the body) and
 * before {@link BuildOwner}. Rejects the create with a 409 via {@link OwnerEmailDuplicateException}
 * when the lower-cased email already belongs to any other owner. Email is optional, so a body with no
 * email is left alone. Existing owners are compared on their email lower-cased the same way, so
 * stored values with incidental casing still match.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerEmailDuplicateException {
        // ValidateOwner has already lower-cased the request email; only stored owners need it.
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && email.equals(existingEmail.toLowerCase())) {
                throw new OwnerEmailDuplicateException(email);
            }
        }
    }
}
