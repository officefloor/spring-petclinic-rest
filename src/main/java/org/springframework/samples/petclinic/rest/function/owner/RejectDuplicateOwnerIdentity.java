package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * Rejects a create whose {@link OwnerIdentity identity key} — normalized telephone, lower-cased
 * email and surname Soundex — already belongs to an existing, active owner, with a 409. Only
 * {@link Owners#active active} owners count, so a soft-deleted owner never blocks a re-create.
 *
 * <p>Runs after the request's telephone and email have been normalized and the email-domain
 * blocklist applied, so the key is computed from the same canonical values a stored owner
 * carries. A create that clears this check but merely shares a surname Soundex and postcode is
 * flagged a soft match later by {@link AssignOwnerPossibleDuplicate}, not rejected here.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), request.getLastName());
        for (Owner existing : Owners.active(ownerRepository.findAll())) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateOwnerException(existing.getId());
            }
        }
    }
}
