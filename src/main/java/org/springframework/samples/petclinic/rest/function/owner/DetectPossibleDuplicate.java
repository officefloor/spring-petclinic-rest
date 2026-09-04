package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a new owner as a soft duplicate when an existing owner shares its last name
 * (compared case-insensitively) and postcode but has a different telephone — a likely
 * household match that is not a hard duplicate. Records {@code possibleDuplicateOf} with
 * the matching owner's id, or leaves the flag false when there is no such match. Runs
 * before the new owner is saved, so it inspects only pre-existing owners.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = key(owner.getLastName());
        String postcode = key(owner.getPostcode());
        owner.setPossibleDuplicate(false);
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(key(existing.getLastName())) && postcode.equals(key(existing.getPostcode()))
                    && !key(owner.getTelephone()).equals(key(existing.getTelephone()))) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
