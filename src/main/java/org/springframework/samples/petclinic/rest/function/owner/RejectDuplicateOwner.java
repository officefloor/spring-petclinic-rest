package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Identity duplicate detection for create-owner — "the duplicate block", now the single duplicate
 * check (the old household-duplicate 409 no longer exists). Detection keys off the full
 * {@link IdentityKey} — the SHA-256 hex over {@code normalizedTelephone + '|' + lowerEmail + '|' +
 * soundex(lastName)}: a create is rejected via {@link DuplicateIdentityException} (turned into a 409 by
 * the global handler) only when its identityKey collides with an existing owner's. Because the
 * telephone is part of the key, distinct members of the same household — same last name and postcode
 * but different telephones — derive different identityKeys and coexist; only a genuine identity
 * collision is rejected. Such household members are instead flagged by the soft-match
 * ({@link DetectPossibleDuplicate}).
 *
 * <p>A request that sets {@code sharesHousehold} true bypasses this block: it is a declared household
 * member and is created. Runs before the owner is built and saved, so
 * {@link OwnerRepository#findAll()} sees only the owners that existed before this create.
 */
public class RejectDuplicateOwner {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the duplicate block
        }
        String identityKey = IdentityKey.of(request.getTelephone(), request.getEmail(),
                request.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the duplicate check
            }
            String existingKey = IdentityKey.of(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
