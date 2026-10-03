package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's soft-match flags. A new owner that is not a hard duplicate (the
 * hard-duplicate 409 is already decided by {@link EnsureUniqueIdentity}) is still flagged
 * as a possible duplicate when it shares an existing owner's last name (compared
 * case-insensitively) and postcode while carrying a different telephone: in that case
 * {@code possibleDuplicate} is {@code true} and {@code possibleDuplicateOf} is the matching
 * owner's id. Otherwise {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} is null.
 *
 * <p>Runs before the owner is saved and over the existing owners only (the new owner is not
 * yet persisted); when more than one existing owner matches, the lowest id wins so the
 * result is deterministic.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = OwnerNames.normalize(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        if (postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null) {
                    continue;
                }
                if (lastName.equals(OwnerNames.normalize(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())
                        && !postcode.isBlank()
                        && !Objects.equals(telephone, existing.getTelephone())) {
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
