package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create request that exactly duplicates an existing owner's identity. Identity is
 * the single {@link IdentityKeys#forFields(String, String, String) identity key} — the
 * SHA-256 of {@code normalizedTelephone + lowerEmail + soundex(lastName)} — so two owners
 * collide (409) only when all three components match. Two owners with the same last name and
 * postcode but different telephones no longer collide here: they are created and left for
 * {@link AssignPossibleDuplicate} to flag as a soft match. A 409 is reported by
 * {@link org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityExceptionHandler}.
 *
 * <p>The email-domain blocklist is applied earlier by {@link RequireOwnerFields} (a 400), so
 * a blocklisted request never reaches this check. Soft-deleted owners are ignored. Setting
 * {@code sharesHousehold} bypasses this block entirely: the owner is then created as a
 * declared household member (and, being declared, is not flagged as a possible duplicate).
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member bypasses the duplicate block
        }
        String identityKey = IdentityKeys.forFields(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new create
            }
            if (identityKey.equals(IdentityKeys.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
