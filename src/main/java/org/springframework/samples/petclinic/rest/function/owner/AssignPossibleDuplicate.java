package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (non-hard) duplicate on the owner being created.
 *
 * <p>A hard duplicate — an existing owner with the same whole {@code identityKey} — has
 * already been rejected with 409 by {@link EnsureUniqueIdentity}. The soft-match key is
 * a phonetic near-match: the last names share a {@link Soundex} code and the postcodes
 * match, while the whole {@code identityKey} differs (so it is not a hard duplicate).
 * Because the telephone is part of the identity key, two owners with the same last name
 * and postcode but different telephones reach this step and are flagged here rather than
 * rejected.
 *
 * <p>A <em>declared</em> household member ({@code sharesHousehold: true}) is not a
 * suspected duplicate, so it is never flagged. For any other owner the soft match, when
 * present, sets {@code possibleDuplicate} true and {@code possibleDuplicateOf} to the
 * matching owner's id (the lowest id when several match, for determinism); otherwise
 * {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is absent.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline, so the new owner is not yet persisted and is never
 * compared against itself; the flags are persisted with the new owner and returned by
 * later reads. It mutates the built {@link Owner} in place (see {@code @Val} semantics).
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String lastNameCode = Soundex.encode(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a possible-duplicate match
            }
            if (lastNameCode.equals(Soundex.encode(existing.getLastName()))
                    && postcode.equals(existing.getPostcode())
                    && !identityKey.equals(existing.getIdentityKey())
                    && (match == null || existing.getId() < match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
