package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

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
        String lastName = normalize(owner.getLastName());
        String postcode = normalize(owner.getPostcode());
        if (postcode.isEmpty()) {
            return; // no postcode to share on
        }
        String telephone = E164Telephone.normalizeOrNull(owner.getTelephone());
        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a possible duplicate
            }
            if (!normalize(existing.getLastName()).equals(lastName)) {
                continue;
            }
            if (!normalize(existing.getPostcode()).equals(postcode)) {
                continue;
            }
            String existingTelephone = E164Telephone.normalizeOrNull(existing.getTelephone());
            if (java.util.Objects.equals(telephone, existingTelephone)) {
                continue; // same telephone is not a soft match (it would be a hard duplicate)
            }
            if (matchId == null || existing.getId() < matchId) {
                matchId = existing.getId();
            }
        }
        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
