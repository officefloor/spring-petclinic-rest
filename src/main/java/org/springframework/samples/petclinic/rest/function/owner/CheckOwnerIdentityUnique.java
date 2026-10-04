package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}. It rejects a request with a 409 via
 * {@link DuplicateIdentityException} for an <b>exact identity duplicate</b>: a request whose whole
 * {@link OwnerIdentity identityKey} (normalised telephone, email and household together) equals an
 * existing owner's — a genuinely identical owner is always a duplicate.
 *
 * <p>Sharing a household is no longer a duplicate on its own: several members may belong to the
 * same household (same {@code lastName} and {@code postcode}, see {@link OwnerHousehold}) as long
 * as they differ in telephone or email, so their identity keys differ. A new member's standing is
 * instead bounded by the household level ceiling applied later in the pipeline (see
 * {@link CapMembershipLevel}), not by rejecting the create.
 *
 * <p>The household id is derived from {@code (lastName, postcode)} for both the request and each
 * existing owner, so the comparison is independent of any stored id. Runs after
 * {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} (so the request's telephone and
 * email are in their stored, canonical forms) and before {@link BuildOwner}.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerHousehold.deriveId(request);
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a duplicate
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
