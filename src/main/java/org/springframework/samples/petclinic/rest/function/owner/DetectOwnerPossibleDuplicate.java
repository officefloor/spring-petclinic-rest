package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the new owner as a possible (soft) duplicate. A hard duplicate — a matching whole
 * {@link Owner#getIdentityKey() identityKey} — has already been rejected 409 by
 * {@link CheckOwnerIdentityUnique}, so by the time this step runs the owner is definitely being
 * created. It is nonetheless a <em>possible</em> duplicate when an earlier owner shares its lastName
 * (compared case-insensitively) and postcode but with a different telephone.
 *
 * <p>Runs after {@link BuildOwner} has created the entity and before {@link SaveOwner} persists it,
 * scanning only pre-existing (already persisted) owners — the new owner has no id yet and is skipped.
 * The earliest such match (lowest id) is recorded: {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to that owner's id; otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left null. Both are stored on the built owner so the saved and
 * returned record carries them.
 */
public class DetectOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        if (lastName != null && postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null
                        || existing.getId().equals(owner.getId())) {
                    continue;
                }
                if (lastName.equalsIgnoreCase(existing.getLastName())
                        && postcode.equals(existing.getPostcode())
                        && (telephone == null || !telephone.equals(existing.getTelephone()))) {
                    if (match == null || existing.getId() < match.getId()) {
                        match = existing;
                    }
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
