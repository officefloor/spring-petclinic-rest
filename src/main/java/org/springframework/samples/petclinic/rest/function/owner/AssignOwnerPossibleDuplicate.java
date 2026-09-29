package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-match flags. A create that survives the duplicate block
 * ({@link RequireUniqueIdentity}) may still resemble an existing owner: when it shares an existing
 * owner's household (the deterministic {@link HouseholdKey#householdId} over {@code (lastName, postcode)})
 * but carries a different {@code telephone}, it is still created but flagged as a possible duplicate. In
 * that case {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching owner's
 * id (the earliest such owner by id when several match); otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left null.
 *
 * <p>A <em>declared</em> household member — one created with {@code sharesHousehold} — is never flagged:
 * it deliberately joined the household, so it is not a suspected duplicate.
 *
 * <p>Runs after {@link BuildOwner} — so the built owner's telephone is already normalized — and before
 * {@link SaveOwner}, so every owner considered is an existing one.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        // A declared household member is not a suspected duplicate.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }

        String householdId = HouseholdKey.householdId(owner);
        String telephone = owner.getTelephone();

        Optional<Owner> match = ownerRepository.findAll().stream()
                .filter(DuplicateCandidates::isCandidate)
                .filter(existing -> householdId.equals(HouseholdKey.householdId(existing))
                        && !Objects.equals(existing.getTelephone(), telephone))
                .min(Comparator.comparingInt(Owner::getId));

        if (match.isPresent()) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.get().getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
