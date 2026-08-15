package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that flags a soft duplicate. The create has already passed
 * {@link RequireUniqueIdentity}, so its {@link OwnerIdentityKey} is unique. This step marks the
 * owner when an existing, non-deleted owner has a DIFFERENT identityKey yet shares both this owner's
 * {@link Soundex} surname code and postcode — a phonetically-identical surname at the same postcode
 * reached, typically, from a different telephone.
 *
 * <p>A declared household member — one whose request set {@code sharesHousehold=true} — is not a
 * suspected duplicate, so it is never flagged. Otherwise, when such an owner exists,
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to that owner's id — the
 * earliest (lowest-id) match when several qualify; otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left unset. Runs before {@link SaveOwner}, so the owner being
 * created is not matched against itself. An owner with no postcode never soft-matches.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = normalizePostcode(owner.getPostcode());
        if (postcode.isEmpty()) {
            return; // an owner with no postcode has no household to match against
        }
        String soundex = Soundex.encode(owner.getLastName());
        String identityKey = OwnerIdentityKey.forOwner(owner);
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a duplicate
            }
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                continue; // an exact identity match is a hard duplicate, not a soft one
            }
            if (!soundex.equals(Soundex.encode(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(normalizePostcode(existing.getPostcode()))) {
                continue;
            }
            if (match == null || (existing.getId() != null && match.getId() != null
                    && existing.getId() < match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String normalizePostcode(String postcode) {
        return postcode == null ? "" : postcode.trim();
    }
}
