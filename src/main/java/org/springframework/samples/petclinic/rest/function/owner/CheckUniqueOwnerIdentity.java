package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single duplicate check on {@code POST /api/owners}, keyed off the deterministic
 * {@link OwnerHousehold} householdId. Rejects with 409 via {@link DuplicateIdentityException} when
 * the new owner's whole {@link OwnerIdentityKey} ({@code <telephone>|<email>|<householdId>}) equals
 * an existing owner's — an exact duplicate.
 *
 * <p>Because the telephone is part of the key, members of the same household (same
 * {@code (lastName, postcode)}, hence the same computed householdId) with different telephones have
 * different keys and are both allowed — the household can hold several members. Such a soft match is
 * not rejected here; it is flagged instead by {@link AssignOwnerPossibleDuplicate}, and its
 * membership level is capped by the household ceiling.
 *
 * <p>Runs before {@link BuildOwner} so no owner is created on conflict, and resolves the householdId
 * the request would receive so its key matches the one assigned and returned afterwards.
 */
public class CheckUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerHousehold.householdId(request);
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new registration
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
