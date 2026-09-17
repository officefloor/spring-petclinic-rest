package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: records whether the new owner is a possible duplicate of
 * an existing one — sharing the same last name and postcode but a different telephone (see
 * {@link PossibleDuplicate}). Such an owner is still created; the matching owner's id is
 * stored in {@code possibleDuplicateOf} so a later read can flag it.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} has already rejected hard duplicates, so any
 * match found here is genuinely a softer one. When several existing owners match, the
 * earliest-created (lowest id) is recorded; no match leaves the field unset.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (PossibleDuplicate.matches(existing, owner)) {
                Integer id = existing.getId();
                if (id != null && (matchId == null || id < matchId)) {
                    matchId = id;
                }
            }
        }
        owner.setPossibleDuplicateOf(matchId);
    }
}
