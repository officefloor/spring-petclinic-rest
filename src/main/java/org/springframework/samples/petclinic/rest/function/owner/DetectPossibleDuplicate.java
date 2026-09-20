package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the new owner as a possible duplicate of an existing one. A create that reaches this
 * step has already cleared the hard identity check ({@link EnsureUniqueIdentity}); it is a
 * <em>possible</em> duplicate when it shares an existing owner's last name and postcode but was
 * given a different telephone (see {@link PossibleDuplicates}). The matching owner's id is
 * recorded so the response can point at it; otherwise the owner is flagged as no duplicate.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer matchId = PossibleDuplicates.matchFor(owner, ownerRepository).orElse(null);
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
