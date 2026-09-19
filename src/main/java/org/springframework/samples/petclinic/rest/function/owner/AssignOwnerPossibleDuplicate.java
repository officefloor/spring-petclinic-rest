package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate. A new owner that is not a hard duplicate (its identity key is
 * unique, see {@link EnsureUniqueIdentityKey}) may still resemble an existing owner: when
 * it shares an existing owner's last name and postcode but has a different telephone it is
 * created anyway and marked as a possible duplicate of that owner. Runs after
 * {@link EnsureUniqueIdentityKey} (so hard duplicates are already rejected) and before
 * {@link SaveOwner} persists the flags. When no such owner exists the new owner keeps its
 * default of not being a possible duplicate; when several match, the earliest (lowest id)
 * is recorded. An owner with no postcode can never be a possible duplicate.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        if (owner.getPostcode() == null) {
            return;
        }
        ownerRepository.findByLastName(owner.getLastName()).stream()
                .filter(existing -> existing.getLastName() != null
                        && existing.getLastName().equalsIgnoreCase(owner.getLastName())
                        && owner.getPostcode().equals(existing.getPostcode())
                        && !owner.getTelephone().equals(existing.getTelephone()))
                .min(Comparator.comparingInt(Owner::getId))
                .ifPresent(match -> {
                    owner.setPossibleDuplicate(true);
                    owner.setPossibleDuplicateOf(match.getId());
                });
    }
}
