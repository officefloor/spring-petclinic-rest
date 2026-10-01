package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the owner as a <em>possible</em> (soft) duplicate of an existing owner.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link FlagOwnerBulkSignup} and before
 * {@link SaveOwner}, so the new owner is not yet persisted and therefore not compared against
 * itself. By this point the request has already cleared {@link CheckOwnerHouseholdUnique}, so it is
 * not a <em>hard</em> identity duplicate. A soft match is a weaker signal: an existing owner that
 * shares this owner's last name (case-insensitive) and postcode but carries a <em>different</em>
 * telephone. People who really are distinct but look alike — same family name, same postcode, new
 * phone — are still created, but flagged for review.
 *
 * <p>When such a match is found, {@code possibleDuplicate} is set to {@code true} and
 * {@code possibleDuplicateOf} to the matching owner's id (the lowest id when several match, so the
 * result is deterministic); otherwise {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} stays null. A postcode is required on both sides for a match — an
 * owner with no postcode never soft-matches. The values are frozen at creation time and returned
 * unchanged on later reads.
 *
 * <p>A <em>declared</em> household member — a request that opted in with {@code sharesHousehold: true}
 * to join an existing household (same last name and postcode) — is never flagged: a declared member
 * is not a suspected duplicate. Such a request is left with {@code possibleDuplicate = false}.
 */
public class FlagOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();

        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                    continue;
                }
                if (equalsIgnoreCase(existing.getLastName(), lastName)
                        && postcode.equals(existing.getPostcode())
                        && !equalsIgnoreCase(existing.getTelephone(), telephone)) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }

        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
