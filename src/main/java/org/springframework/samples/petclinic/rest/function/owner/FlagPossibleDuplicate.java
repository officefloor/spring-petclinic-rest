package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags a soft ("possible") duplicate. The hard-duplicate
 * check ({@link EnsureUniqueIdentity}) has already rejected an exact identity collision with 409, so
 * this owner is being created. This step then marks it as a possible duplicate when it is not a hard
 * duplicate yet still shares an existing owner's last name and postcode with a <em>different</em>
 * telephone.
 *
 * <p>When such an existing owner is found the new owner's {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to that owner's id (the lowest matching id when several match);
 * otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is null. A missing
 * postcode never matches, since the rule keys on last name <em>and</em> postcode. Runs after
 * {@link EnsureUniqueIdentity} and before {@link SaveOwner}, so the flags are persisted with the owner.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String telephone = owner.getTelephone();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // the owner being created is not its own possible duplicate
            }
            if (!normalize(existing.getLastName()).equals(lastName)) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (telephone != null && telephone.equals(existing.getTelephone())) {
                continue; // a shared telephone is a hard-duplicate concern, not a soft match
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
