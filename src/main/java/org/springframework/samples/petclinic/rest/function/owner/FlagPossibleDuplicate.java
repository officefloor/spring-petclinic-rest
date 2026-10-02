package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Populates the response-only {@link Owner#isPossibleDuplicate() possibleDuplicate} and
 * {@link Owner#getPossibleDuplicateOf() possibleDuplicateOf} fields from the shared
 * {@link PossibleDuplicate} rule: true, with the matching owner id, when this owner shares an
 * existing owner's last name and postcode with a different telephone; otherwise false with no
 * id. Mutates the loaded owner in place so the responder's mapper carries the flag onto the DTO.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer match = PossibleDuplicate.matchOf(owner, ownerRepository);
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match);
    }
}
