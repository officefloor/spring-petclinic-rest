package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (possible) duplicate. The new owner is not a hard duplicate — an existing owner with
 * the same {@code identityKey} would already have been rejected with 409 by
 * {@link CheckOwnerIdentityUnique} — but it may still be a <em>likely</em> duplicate of an existing
 * owner: a matching {@link Soundex} of the {@code lastName} and the same {@code postcode}, yet a
 * different {@code identityKey} (e.g. a different telephone).
 *
 * <p>A <em>declared</em> household member ({@code sharesHousehold = true}) is never a suspected
 * duplicate: it opted in explicitly, so it is left with {@code possibleDuplicate = false}. Otherwise,
 * when a matching existing owner is found, the new owner is still created but carries
 * {@code possibleDuplicate = true} and {@code possibleDuplicateOf} set to that existing owner's id;
 * when several match, the earliest (lowest id) is used for a deterministic result. Otherwise
 * {@code possibleDuplicate = false} and {@code possibleDuplicateOf} is left {@code null}.
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} (hard duplicates already rejected) and before
 * {@link SaveOwner}, so the scan sees only the owners already persisted, not the new owner itself.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return; // no postcode to match on -> never a possible duplicate
        }
        String lastNameSoundex = Soundex.encode(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a possible duplicate
            }
            if (!lastNameSoundex.equals(Soundex.encode(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                continue; // same identity is not a soft match (a hard match was already rejected)
            }
            if (match == null || lowerId(existing.getId(), match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static boolean lowerId(Integer candidate, Integer current) {
        if (candidate == null) {
            return false;
        }
        return current == null || candidate < current;
    }
}
