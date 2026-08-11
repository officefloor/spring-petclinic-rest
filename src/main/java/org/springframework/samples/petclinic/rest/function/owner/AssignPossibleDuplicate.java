package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match detection for {@code POST /api/owners}: an owner that is <em>not</em> a hard
 * ({@link OwnerIdentityKey identityKey}) duplicate but nonetheless shares an existing owner's
 * {@code lastName} and {@code postcode} while having a different telephone is still created, and
 * flagged. Sets {@code possibleDuplicate} to {@code true} and {@code possibleDuplicateOf} to the
 * matching owner's id when such a match exists; otherwise {@code possibleDuplicate} is {@code false}
 * and {@code possibleDuplicateOf} is left absent.
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} (so a hard duplicate has already been rejected with
 * 409 and never reaches here) and before {@link SaveOwner} (so the not-yet-saved owner is not
 * compared against itself). When more than one existing owner matches, the earliest (lowest id) is
 * reported, for a deterministic result.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (lastName != null && postcode != null && !postcode.isBlank()) {
            String telephone = normalizeTelephone(owner.getTelephone());
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue;
                }
                if (existing.getId() == null) {
                    continue;
                }
                if (!lastName.equalsIgnoreCase(existing.getLastName())) {
                    continue;
                }
                if (!postcode.equals(existing.getPostcode())) {
                    continue;
                }
                // A shared telephone is a hard-duplicate concern, not a soft match.
                if (telephone.equals(normalizeTelephone(existing.getTelephone()))) {
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

    private static String normalizeTelephone(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = E164Telephone.normalize(raw);
        return normalized != null ? normalized : raw.trim();
    }
}
