package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match step in the create-owner pipeline. Runs after {@link BuildOwner} (so the owner carries
 * its normalized telephone, lastName and postcode) and before {@link SaveOwner}. It is reached only
 * for owners that already passed {@link EnsureUniqueIdentity}, i.e. that are NOT hard duplicates.
 *
 * <p>An owner is a <em>possible</em> duplicate when an existing owner shares its {@code lastName}
 * and {@code postcode} but has a different {@code telephone}. When such an owner exists the new
 * owner is still created, but flagged with {@code possibleDuplicate = true} and
 * {@code possibleDuplicateOf} set to the matching owner's id (the lowest id when several match).
 * Otherwise {@code possibleDuplicate} is set to {@code false} and no match id is recorded.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (lastName == null || postcode == null || postcode.isBlank()) {
            return; // a shared lastName+postcode is required to be a possible duplicate
        }

        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null) {
                continue;
            }
            if (lastName.equals(existing.getLastName())
                    && postcode.equals(existing.getPostcode())
                    && !equalsTelephone(telephone, existing.getTelephone())) {
                if (matchId == null || existing.getId() < matchId) {
                    matchId = existing.getId();
                }
            }
        }

        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }

    private static boolean equalsTelephone(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
