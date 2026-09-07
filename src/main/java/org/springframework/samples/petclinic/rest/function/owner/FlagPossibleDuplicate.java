package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags a soft duplicate. A new owner that is not a hard
 * duplicate (see {@link RequireUniqueIdentity}) but shares an existing owner's last name and
 * postcode while having a different telephone is a <em>possible</em> duplicate: it is still created,
 * but {@code possibleDuplicate} is set {@code true} and {@code possibleDuplicateOf} to the matching
 * owner's id. Otherwise {@code possibleDuplicate} is {@code false} and {@code possibleDuplicateOf} is
 * left unset.
 *
 * <p>Last name is compared case-insensitively; postcode must be present and equal; the telephone is
 * compared in its canonical form so an owner with the same telephone (a hard duplicate) is not
 * flagged here. When several existing owners match, the one with the lowest id (the earliest) is
 * reported. Runs after {@link BuildOwner} so the owner's telephone is already normalized, and before
 * {@link SaveOwner} so the owner being created is not compared against itself.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = OwnerIdentity.normalizedTelephone(owner.getTelephone());
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || !lastName.equalsIgnoreCase(existing.getLastName())
                        || !postcode.equals(existing.getPostcode())) {
                    continue;
                }
                if (telephone.equals(OwnerIdentity.normalizedTelephone(existing.getTelephone()))) {
                    continue; // same telephone: a hard duplicate, not a possible one
                }
                if (matchId == null || existing.getId() < matchId) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
