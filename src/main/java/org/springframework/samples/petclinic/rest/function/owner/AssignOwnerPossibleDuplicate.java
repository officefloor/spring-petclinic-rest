package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;
import java.util.Optional;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Flags a soft duplicate: an owner whose {@link OwnerIdentity#key identity key} differs from every
 * existing owner (so it is not a hard duplicate, already rejected upstream) but which shares an
 * existing owner's {@code soundex(lastName)} and postcode. Records the match as
 * {@code possibleDuplicate} true with {@code possibleDuplicateOf} set to the matched owner's id,
 * else {@code possibleDuplicate} false. Runs before the new owner is persisted so it is not compared
 * against itself.
 *
 * <p>An owner that declared itself a household member with {@code sharesHousehold=true} is never
 * flagged: it is a known member of the household, not a suspected duplicate of it.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        Optional<Owner> match = softMatch(request, owner, ownerRepository);
        owner.setPossibleDuplicate(match.isPresent());
        owner.setPossibleDuplicateOf(match.map(Owner::getId).orElse(null));
    }

    private static Optional<Owner> softMatch(OwnerFieldsDto request, Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return Optional.empty(); // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return Optional.empty(); // a shared postcode is required to soft-match
        }
        String identityKey = OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        String soundex = Soundex.encode(owner.getLastName());
        return ownerRepository.findAll().stream()
                .filter(existing -> !existing.isDeleted()) // a soft-deleted owner is not a duplicate match
                .filter(existing -> postcode.equals(existing.getPostcode()))
                .filter(existing -> soundex.equals(Soundex.encode(existing.getLastName())))
                .filter(existing -> !identityKey.equals(OwnerIdentity.key(existing.getTelephone(),
                        existing.getEmail(), existing.getLastName())))
                .min(Comparator.comparing(Owner::getId));
    }
}
