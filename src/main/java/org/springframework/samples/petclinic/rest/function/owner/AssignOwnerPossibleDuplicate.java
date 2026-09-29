package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.Strings;

/**
 * Assigns the owner's soft-match flags. A create that survives the hard duplicate check
 * ({@link RequireUniqueIdentity}) may still resemble an existing owner: when it shares an existing
 * owner's {@code lastName} (compared case-insensitively) and {@code postcode} but carries a different
 * {@code telephone}, it is still created but flagged as a possible duplicate. In that case
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching owner's id
 * (the earliest such owner by id when several match); otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left null.
 *
 * <p>Runs after {@link BuildOwner} — so the built owner's telephone is already normalized — and before
 * {@link SaveOwner}, so every owner considered is an existing one.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();

        Optional<Owner> match = (postcode == null || postcode.isBlank())
                ? Optional.empty()
                : ownerRepository.findAll().stream()
                        .filter(existing -> Strings.equalsIgnoreCase(existing.getLastName(), lastName)
                                && postcode.equals(existing.getPostcode())
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
