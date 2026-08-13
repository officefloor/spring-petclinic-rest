package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single duplicate check on {@code POST /api/owners}, keyed off the deterministic
 * {@link OwnerHousehold} householdId. Rejects with 409 via {@link DuplicateIdentityException} when
 * either:
 * <ul>
 * <li>the new owner's whole {@link OwnerIdentityKey} ({@code <telephone>|<email>|<householdId>})
 * equals an existing owner's — an exact duplicate; or</li>
 * <li>an existing owner shares the new owner's household (same {@code (lastName, postcode)}, hence
 * the same computed householdId) — a household duplicate. This is bypassed when the request opts in
 * with {@code sharesHousehold} true, in which case the owner is created as a declared household
 * member.</li>
 * </ul>
 * Runs before {@link BuildOwner} so no owner is created on conflict, and resolves the householdId
 * the request would receive so its key matches the one assigned and returned afterwards.
 */
public class CheckUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerHousehold.householdId(request);
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(), householdId);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new registration
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId.equals(OwnerHousehold.householdId(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
