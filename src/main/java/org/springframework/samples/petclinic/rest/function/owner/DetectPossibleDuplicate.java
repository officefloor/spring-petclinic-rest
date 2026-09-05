package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match duplicate detection for create-owner. Runs after the identity duplicate block
 * ({@link RejectDuplicateOwner}), which rejects only an exact {@link IdentityKey} collision. A soft
 * match is the near-miss the identity key deliberately lets through: an owner whose identityKey
 * <em>differs</em> from an existing owner's yet shares that owner's {@code soundex(lastName)} and
 * postcode — for example two members of one household with different telephones (different telephone →
 * different identityKey). Such an owner is flagged: {@code possibleDuplicate} true and
 * {@code possibleDuplicateOf} the matching owner's id (the lowest id when several match).
 *
 * <p>A request that sets {@code sharesHousehold} true is a declared household member, not a suspected
 * duplicate, so it is never flagged: {@code possibleDuplicate} false and {@code possibleDuplicateOf}
 * null. Soft-deleted owners are ignored, as by the duplicate block. Runs before the owner is saved, so
 * {@link OwnerRepository#findAll()} sees only owners that existed before this create.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // declared household member — not a suspected duplicate
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the duplicate check
            }
            String existingKey = IdentityKey.of(existing.getTelephone(), existing.getEmail(),
                    existing.getLastName());
            if (identityKey.equals(existingKey)) {
                continue; // same identityKey is a hard duplicate (409), not a soft match
            }
            if (soundex.equals(Soundex.of(existing.getLastName()))
                    && Objects.equals(owner.getPostcode(), existing.getPostcode())) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
