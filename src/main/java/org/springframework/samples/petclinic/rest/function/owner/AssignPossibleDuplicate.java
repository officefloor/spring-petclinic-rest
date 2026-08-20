package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Raises the soft-match {@code possibleDuplicate} flag. A create request that is not a hard duplicate
 * (see {@link CheckOwnerIdentityUnique}) can still resemble an existing owner: when its
 * {@link OwnerIdentityKey identityKey} <em>differs</em> from an existing owner's but the two share a
 * last-name {@link Soundex} code and a postcode, the new owner is created with {@code possibleDuplicate}
 * true and {@code possibleDuplicateOf} set to the matching owner's id. Otherwise {@code possibleDuplicate}
 * is false and {@code possibleDuplicateOf} absent.
 *
 * <p>Because the telephone is part of the identity key, two owners with the same last name and postcode
 * but different telephones are no longer a hard duplicate — the second reaches this step and is flagged
 * here. A declared household member ({@code sharesHousehold}) is not a suspected duplicate and is never
 * flagged.
 *
 * <p>Runs after {@link BuildOwner} (telephone, last name and postcode are in their stored form) and
 * after {@link CheckOwnerIdentityUnique}, and before {@link SaveOwner} so the flag is compared only
 * against existing owners. When several existing owners match, the one with the lowest id wins for a
 * deterministic result.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = normalize(owner.getPostcode());
        if (postcode.isEmpty()) {
            return; // no postcode to match on
        }
        String soundex = Soundex.of(owner.getLastName());
        if (soundex.isEmpty()) {
            return; // no last name to match on
        }
        String identityKey = OwnerIdentityKey.of(owner);
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never a duplicate of itself
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is ignored by the soft-match check
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                continue; // an equal key is a hard duplicate, not a soft match (and never reaches here)
            }
            if (soundex.equals(Soundex.of(existing.getLastName()))
                    && postcode.equals(normalize(existing.getPostcode()))) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
