package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Consolidated duplicate check: the former telephone, email and household checks are now a single
 * equality on the identity key ({@link IdentityKey}). Two owners are duplicates only when their
 * telephone and email both match, so members of the same household with different telephones have
 * different keys and are both allowed. Rejects a collision with a 409 via
 * {@link DuplicateIdentityException}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String telephone = request.getTelephone();
        String email = normalizeEmail(request.getEmail());
        for (Owner owner : ownerRepository.findAll()) {
            if (!owner.isDeleted() && telephone.equals(owner.getTelephone())
                    && email.equals(normalizeEmail(owner.getEmail()))) {
                throw new DuplicateIdentityException(telephone + "|" + email);
            }
        }
    }

    private static String normalizeEmail(String email) {
        return email == null || email.isBlank() ? "" : email.toLowerCase(Locale.ROOT);
    }
}
