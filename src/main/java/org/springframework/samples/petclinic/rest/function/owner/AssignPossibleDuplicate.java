package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Raises the soft-match {@code possibleDuplicate} flag. A create request that is not a hard
 * duplicate (its whole {@link OwnerIdentityKey} is unique — see {@link CheckOwnerIdentityUnique})
 * can still resemble an existing owner: when it shares an existing owner's lastName (compared
 * case-insensitively) and postcode but has a <em>different</em> telephone, the new owner is still
 * created with {@code possibleDuplicate} true and {@code possibleDuplicateOf} set to the matching
 * owner's id. Otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} absent.
 *
 * <p>Runs after {@link BuildOwner} (telephone, lastName and postcode are in their stored form) and
 * after {@link CheckOwnerIdentityUnique} (so a hard duplicate has already been rejected), and before
 * {@link SaveOwner} so the flag is compared only against existing owners. When several existing
 * owners match, the one with the lowest id wins for a deterministic result.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String postcode = normalize(owner.getPostcode());
        String telephone = normalize(owner.getTelephone());
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (postcode.isEmpty()) {
            return; // no postcode to match on
        }
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never a duplicate of itself
            }
            if (lastName.equals(normalize(existing.getLastName()))
                    && postcode.equals(normalize(existing.getPostcode()))
                    && !telephone.equals(normalize(existing.getTelephone()))) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
