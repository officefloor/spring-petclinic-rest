package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Consolidated duplicate detection for {@code POST /api/owners}: rejects a create with 409 via
 * {@link DuplicateIdentityException} only when the new owner's WHOLE {@code identityKey} (see
 * {@link IdentityKey}) — the SHA-256 hex over {@code normalizedTelephone|lowerEmail|soundex(lastName)}
 * — equals an existing owner's, i.e. the same telephone, email and phonetic surname. The identityKey
 * is the single identity rule: the computed householdId plays no part here, so two owners with the
 * same surname and postcode but different telephones have different keys and are both allowed (they
 * are surfaced as a soft match by {@link FlagPossibleDuplicate}). This is what lets a household
 * accumulate members for the membership-level cap (see {@link CapMembershipLevel}). Only a truly
 * identical identity is a conflict.
 *
 * <p>Runs after {@link AssignHousehold} and before {@link SaveOwner}. The email-domain blocklist
 * ({@link RejectDisposableEmailDomain}) has already applied earlier in the pipeline. Soft-deleted
 * owners no longer block a create.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // the owner being created is not yet its own duplicate
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            if (IdentityKey.of(existing).equals(identityKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
