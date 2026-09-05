package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create-owner request as a <em>possible</em> (soft) duplicate: one that is not a hard
 * duplicate (see {@link RejectDuplicateIdentity}) yet shares an existing owner's
 * {@code lastName} (compared case-insensitively with surrounding whitespace trimmed) and
 * {@code postcode} while carrying a <em>different</em> telephone (compared in E.164 form, see
 * {@link E164Telephone}). Such an owner is still created; it is merely marked so callers can
 * review it. When a match is found the owner's {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to the matching owner's id (the earliest such owner by id when
 * several match); otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf}
 * is left unset.
 *
 * <p>A create that opts in with {@code sharesHousehold} is a <em>declared</em> household member,
 * not a suspected one, so it is never flagged: {@code possibleDuplicate} stays false.
 *
 * <p>Runs after {@link RejectDuplicateIdentity} (so a hard duplicate has already been rejected)
 * and within the create transaction, but before {@link SaveOwner}, so the new owner is not yet
 * persisted and cannot match itself.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        if (normalize(owner.getPostcode()).isEmpty()) {
            return; // no postcode to share on
        }
        Integer matchId = earliestSoftMatch(owner, ownerRepository);
        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }

    /**
     * The id of the earliest existing owner (lowest id) that {@code owner} is a soft duplicate of,
     * or {@code null} when none matches.
     */
    private static Integer earliestSoftMatch(Owner owner, OwnerRepository ownerRepository) {
        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (!isSoftMatch(owner, existing)) {
                continue;
            }
            if (matchId == null || existing.getId() < matchId) {
                matchId = existing.getId();
            }
        }
        return matchId;
    }

    /**
     * Whether {@code existing} makes {@code owner} a soft (possible) duplicate: a different,
     * live owner sharing its last name and postcode but carrying a different telephone (the same
     * telephone would be a hard duplicate, already rejected upstream).
     */
    private static boolean isSoftMatch(Owner owner, Owner existing) {
        if (owner.getId() != null && owner.getId().equals(existing.getId())) {
            return false;
        }
        if (existing.isDeleted()) {
            return false; // a soft-deleted owner is not a possible duplicate
        }
        if (!normalize(existing.getLastName()).equals(normalize(owner.getLastName()))) {
            return false;
        }
        if (!normalize(existing.getPostcode()).equals(normalize(owner.getPostcode()))) {
            return false;
        }
        String telephone = E164Telephone.normalizeOrNull(owner.getTelephone());
        String existingTelephone = E164Telephone.normalizeOrNull(existing.getTelephone());
        // same telephone is not a soft match (it would be a hard duplicate)
        return !Objects.equals(telephone, existingTelephone);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
