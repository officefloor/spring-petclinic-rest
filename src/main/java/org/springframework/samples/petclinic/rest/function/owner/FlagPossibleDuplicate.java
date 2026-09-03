package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-match flag: a create that has already cleared the hard identity guard but whose
 * {@code identityKey} still differs from an existing owner sharing its {@code soundex(lastName)}
 * and postcode is recorded as a possible duplicate. Sets {@code possibleDuplicate} true and
 * {@code possibleDuplicateOf} to that owner's id, otherwise false. A declared household member
 * ({@code sharesHousehold} true) is never a suspected duplicate.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            return;
        }
        String soundex = Soundex.of(owner.getLastName());
        String postcode = normalize(owner.getPostcode());
        String key = OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        for (Owner other : ownerRepository.findAll()) {
            if (!postcode.isEmpty() && soundex.equals(Soundex.of(other.getLastName()))
                    && postcode.equals(normalize(other.getPostcode()))
                    && !key.equals(OwnerIdentity.key(other.getTelephone(), other.getEmail(), other.getLastName()))) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(other.getId());
                return;
            }
        }
        owner.setPossibleDuplicate(false);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
