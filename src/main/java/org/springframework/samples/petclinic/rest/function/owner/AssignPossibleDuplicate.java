package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match detection for {@code POST /api/owners}: an owner that is <em>not</em> a hard
 * ({@link OwnerIdentityKey identityKey}) duplicate but nonetheless matches an existing owner's
 * {@code soundex(lastName)} and {@code postcode} is still created, and flagged. Sets
 * {@code possibleDuplicate} to {@code true} and {@code possibleDuplicateOf} to the matching owner's
 * id when such a match exists; otherwise {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} is left absent.
 *
 * <p>Because the identityKey now folds in the telephone, two owners with the same last name and
 * postcode but different telephones have different keys — they are no longer a hard duplicate but a
 * soft match, caught here. The match is therefore keyed on the same {@link OwnerIdentityKey#soundex
 * Soundex} of the last name that the identityKey uses, plus an exact postcode, and only fires when
 * the two owners' whole identityKeys actually differ (an equal key would already have been rejected
 * with 409 upstream).
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} (so a hard duplicate has already been rejected with
 * 409 and never reaches here) and before {@link SaveOwner} (so the not-yet-saved owner is not
 * compared against itself). When more than one existing owner matches, the earliest (lowest id) is
 * reported, for a deterministic result.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (owner.getLastName() != null && postcode != null && !postcode.isBlank()) {
            String soundex = OwnerIdentityKey.soundex(owner.getLastName());
            String identityKey = OwnerIdentityKey.of(owner);
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue;
                }
                if (existing.getId() == null) {
                    continue;
                }
                if (!soundex.equals(OwnerIdentityKey.soundex(existing.getLastName()))) {
                    continue;
                }
                if (!postcode.equals(existing.getPostcode())) {
                    continue;
                }
                // Equal identityKeys are a hard duplicate (already a 409), not a soft match.
                if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                    continue;
                }
                if (matchId == null || existing.getId() < matchId) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
