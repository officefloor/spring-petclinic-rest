package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-match flag: a create that has already cleared the hard duplicate guards but still shares an
 * existing owner's last name and postcode (compared case-insensitively with collapsed whitespace)
 * while carrying a different telephone is recorded as a possible duplicate. Sets
 * {@code possibleDuplicate} true and {@code possibleDuplicateOf} to that owner's id, otherwise false.
 * A declared household member ({@code sharesHousehold} true) is never a suspected duplicate.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            return;
        }
        String lastName = normalize(owner.getLastName());
        String postcode = normalize(owner.getPostcode());
        for (Owner other : ownerRepository.findAll()) {
            if (!postcode.isEmpty() && lastName.equals(normalize(other.getLastName()))
                    && postcode.equals(normalize(other.getPostcode()))
                    && !equalTelephone(owner.getTelephone(), other.getTelephone())) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(other.getId());
                return;
            }
        }
        owner.setPossibleDuplicate(false);
    }

    private static boolean equalTelephone(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
