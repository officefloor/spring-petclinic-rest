package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check for create-owner, responding 409 via
 * {@link DuplicateOwnerException} on an <b>identity duplicate</b> — an existing owner with the same
 * derived {@code identityKey} (telephone, email and phonetic last name, see {@link OwnerIdentities}),
 * i.e. an exact resubmission of an existing owner.
 *
 * <p>Two owners sharing only a last name and postcode but giving different telephones no longer
 * collide here — telephone is part of the key — so they are admitted and instead flagged as a soft
 * duplicate ({@link FlagOwnerPossibleDuplicate}). Household co-membership is likewise admitted and
 * governed by the membership level ceiling ({@link AssignOwnerMembershipLevel}).
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} so the request
 * telephone is already E.164 and its email lower-cased, and after {@link RequireOwnerEmailAllowed}
 * so a blocklisted email is rejected first.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String identityKey = OwnerIdentities.key(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner never blocks a new one
            }
            if (identityKey.equals(OwnerIdentities.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
