package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the new owner as a possible duplicate of an existing one. A <em>possible</em> duplicate
 * has a last name that sounds alike (same Soundex) and a matching postcode, yet a different
 * {@link IdentityKeys identity key} (see {@link PossibleDuplicates}). The matching owner's id is
 * recorded so the response can point at it; otherwise the owner is flagged as no duplicate.
 *
 * <p>An owner that declared {@code sharesHousehold} is a <em>declared</em> household member, not
 * a suspected duplicate, so it is never flagged even though it resembles one.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request,
            OwnerRepository ownerRepository) {
        Integer matchId = Boolean.TRUE.equals(request.getSharesHousehold()) ? null
                : PossibleDuplicates.matchFor(owner, ownerRepository).orElse(null);
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
