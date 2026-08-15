package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that flags a soft duplicate. The create has already passed
 * {@link RequireUniqueIdentity}, so it is neither a hard identity duplicate nor an undeclared
 * household duplicate. This step marks the owner when an existing owner shares its household (the
 * same deterministic {@code householdId}, i.e. the same lastName and postcode) but has a different
 * telephone (normalized to E.164, see {@link TelephoneE164}).
 *
 * <p>A declared household member — one whose request set {@code sharesHousehold=true} — is not a
 * suspected duplicate, so it is never flagged. Otherwise, when such an owner exists,
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to that owner's id — the
 * earliest (lowest-id) match when several qualify; otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left unset. Runs before {@link SaveOwner}, so the owner being
 * created is not matched against itself. An owner with no household (no postcode) never soft-matches.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        String telephone = normalizeTelephone(owner.getTelephone());
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a duplicate
            }
            if (!householdId.equals(existing.getHouseholdId())) {
                continue;
            }
            if (telephone.equals(normalizeTelephone(existing.getTelephone()))) {
                continue; // same telephone would be a hard duplicate, not a soft match
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

    /** Canonical E.164 telephone, falling back to the raw value when it cannot be parsed. */
    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneE164.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }
}
