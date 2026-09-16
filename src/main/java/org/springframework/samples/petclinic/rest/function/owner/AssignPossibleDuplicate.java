package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Flags a newly built owner as a soft duplicate of an existing owner. Unlike the hard
 * {@link EnsureUniqueIdentity} check (which rejects a matching identity as a 409), a soft
 * match still creates the owner: it fires when the new owner shares an existing owner's
 * postcode and the {@link Soundex} of its last name but has a different {@link IdentityKey}
 * — so a namesake at the same postcode with a different telephone or email is created and
 * flagged rather than rejected. The new owner is stamped with {@code possibleDuplicate=true}
 * and {@code possibleDuplicateOf} set to the matching owner's id; when no match exists the
 * flag is false and the id null.
 *
 * <p>An owner created as a declared household member ({@code sharesHousehold=true}, which
 * bypassed the duplicate block) is never flagged: a declared member is not a suspected
 * duplicate.
 *
 * <p>Runs after {@link BuildOwner} (so the owner's normalized fields are set) and before
 * {@link SaveOwner} (so the new owner is not yet persisted and cannot match itself), and
 * mutates the built owner in place so the flag is stored and returned with it. The first
 * matching existing owner encountered wins.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a duplicate to flag against
            }
            if (isPossibleDuplicate(owner, identityKey, existing)) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
    }

    /**
     * Whether {@code candidate} is a soft duplicate of {@code existing}: the same non-blank
     * postcode and the same last-name Soundex, but a different identity key (so it is not the
     * same identity, which would already have been rejected as a 409).
     */
    private static boolean isPossibleDuplicate(Owner candidate, String candidateKey, Owner existing) {
        return samePostcode(candidate.getPostcode(), existing.getPostcode())
            && sameSoundex(candidate.getLastName(), existing.getLastName())
            && !candidateKey.equals(IdentityKey.of(existing));
    }

    private static boolean samePostcode(String a, String b) {
        return a != null && !a.isBlank() && a.equals(b);
    }

    private static boolean sameSoundex(String a, String b) {
        return Soundex.of(a).equals(Soundex.of(b));
    }
}
