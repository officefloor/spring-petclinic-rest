package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create-owner request that duplicates an existing owner. Duplicate detection is now the
 * single identity key: the derived {@code identityKey} — the SHA-256 hex over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} (see
 * {@link IdentityKey}) — exactly equals an existing (non-deleted) owner's. This one rule expresses
 * what were the separate telephone and email duplicate checks.
 *
 * <p>Because the telephone is part of the key, two owners with the same last name and postcode but
 * different telephones no longer collide here: they are a soft match handled by
 * {@link CheckPossibleDuplicate}, not a 409. The former household-duplicate block (keyed on the
 * computed {@code householdId}) no longer applies.
 *
 * <p>The email-domain blocklist has already run earlier in the pipeline (see
 * {@link CheckEmailDomainAllowed}), so a blocked-domain request never reaches this step.
 *
 * <p>Runs before {@link CheckPossibleDuplicate}/{@link SaveOwner} (so the new owner is not yet
 * among {@code findAll()}). On a match raises {@link DuplicateIdentityException} (409).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner built, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(built);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
