package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.mapper.Soundex;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a new owner as a soft duplicate when an existing owner's identityKey differs but
 * its soundex(lastName) and postcode both match — a likely household match that is not a
 * hard duplicate. Records {@code possibleDuplicateOf} with the matching owner's id, or
 * leaves the flag false when there is no such match. A declared household member
 * ({@code sharesHousehold} true) is never flagged. Runs before the new owner is saved, so
 * it inspects only pre-existing owners.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String identityKey = IdentityKey.of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        String soundex = Soundex.of(owner.getLastName());
        String postcode = key(owner.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            if (!identityKey.equals(IdentityKey.of(existing.getTelephone(), existing.getEmail(), existing.getLastName()))
                    && soundex.equals(Soundex.of(existing.getLastName()))
                    && postcode.equals(key(existing.getPostcode()))) {
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
