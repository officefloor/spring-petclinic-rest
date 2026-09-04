package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Soft-match rule: an owner that is not a hard duplicate but shares another owner's last name and
 * postcode with a different telephone is flagged {@code possibleDuplicate}, with
 * {@code possibleDuplicateOf} set to that owner's id. Mirrors the response-time derivation used for
 * {@link BulkSignup}, so both the create and fetch responders report the same value.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    public static void mark(OwnerDto dto, Owner owner, OwnerRepository ownerRepository) {
        Integer matchId = matchingOwnerId(owner, ownerRepository);
        dto.setPossibleDuplicate(matchId != null);
        dto.setPossibleDuplicateOf(matchId);
    }

    private static Integer matchingOwnerId(Owner owner, OwnerRepository ownerRepository) {
        if (owner.getPostcode() == null) {
            return null;
        }
        String lastName = normalize(owner.getLastName());
        for (Owner other : ownerRepository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId())
                    && lastName.equals(normalize(other.getLastName()))
                    && owner.getPostcode().equals(other.getPostcode())
                    && !owner.getHouseholdId().equals(other.getHouseholdId()) // same household => declared member, not a suspected duplicate
                    && !Objects.equals(owner.getTelephone(), other.getTelephone())) {
                return other.getId();
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
