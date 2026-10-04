package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}. It rejects a request with a 409 via
 * {@link DuplicateIdentityException} for an <b>exact identity duplicate</b>: a request whose whole
 * {@link OwnerIdentity identityKey} (normalised telephone, email and the last name's Soundex
 * together) equals an existing owner's — a genuinely identical owner is always a duplicate.
 *
 * <p>Sharing a household is no longer a duplicate on its own: several members may belong to the
 * same household (same {@code lastName} and {@code postcode}, see {@link OwnerHousehold}) as long
 * as they differ in telephone or email, so their identity keys differ. A new member's standing is
 * instead bounded by the household level ceiling applied later in the pipeline (see
 * {@link CapMembershipLevel}), not by rejecting the create; a non-declared member is additionally
 * flagged as a <em>possible</em> duplicate (see {@link AssignPossibleDuplicate}).
 *
 * <p>The identity key folds the normalised telephone, email and the last name's Soundex together
 * (see {@link OwnerIdentity}); there is no longer any separate household-derived duplicate check.
 * Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} (so the request's
 * telephone and email are in their stored, canonical forms) and before {@link BuildOwner}. The
 * disposable-email blocklist is applied earlier, in {@link NormalizeOwnerEmail}, so it is enforced
 * before this guard runs.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(),
                request.getLastName());
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
