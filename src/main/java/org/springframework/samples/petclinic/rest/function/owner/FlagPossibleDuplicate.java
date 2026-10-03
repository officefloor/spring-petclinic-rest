package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-match flags. A new owner that is not a hard duplicate (the
 * hard-duplicate 409 is already decided by {@link EnsureUniqueHousehold}) is still flagged
 * as a possible duplicate when it shares an existing owner's last name (compared
 * case-insensitively) and postcode while carrying a different telephone: in that case
 * {@code possibleDuplicate} is {@code true} and {@code possibleDuplicateOf} is the matching
 * owner's id. Otherwise {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} is null.
 *
 * <p>A declared household member (the request set {@code sharesHousehold}) is never a
 * suspected duplicate: it deliberately shares a last name and postcode with the rest of its
 * household, so it is left unflagged.
 *
 * <p>Runs before the owner is saved and over the existing owners only (the new owner is not
 * yet persisted); when more than one existing owner matches, the lowest id wins so the
 * result is deterministic.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        String lastName = OwnerNames.normalize(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        // A declared household member deliberately shares a last name and postcode, so it
        // is never a suspected duplicate: skip the scan and leave the flags cleared below.
        if (!Boolean.TRUE.equals(request.getSharesHousehold()) && postcode != null) {
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
