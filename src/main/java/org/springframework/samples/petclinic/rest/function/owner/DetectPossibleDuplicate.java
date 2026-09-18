package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the newly built owner as a possible (soft) duplicate of an existing one. A soft match is an
 * existing owner sharing this owner's last name (case-insensitively) and postcode but reachable on a
 * different telephone — not a hard duplicate (those share the whole {@link Owner#getIdentityKey()
 * identity key} and are rejected by {@link EnsureUniqueIdentity} before this step), so the owner is
 * still created. When one is found the earliest such owner's id is recorded via
 * {@link Owner#setPossibleDuplicateOf(Integer)}, which drives the {@code possibleDuplicate} /
 * {@code possibleDuplicateOf} response fields.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link SaveOwner}, so the flag is persisted
 * with the owner. Owners without a postcode never soft-match.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        ownerRepository.findByLastName(owner.getLastName()).stream()
            .filter(existing -> existing.getLastName().equalsIgnoreCase(owner.getLastName())
                && postcode.equals(existing.getPostcode())
                && existing.getTelephone() != null
                && !existing.getTelephone().equals(owner.getTelephone()))
            .map(Owner::getId)
            .filter(id -> id != null)
            .min(Integer::compareTo)
            .ifPresent(owner::setPossibleDuplicateOf);
    }
}
