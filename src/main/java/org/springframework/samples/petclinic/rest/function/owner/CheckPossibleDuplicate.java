package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Flags the freshly built {@link Owner} as a soft (non-blocking) duplicate before it is saved.
 * A soft duplicate is one that is <em>not</em> a hard duplicate (an exact {@code identityKey}
 * match, already rejected with 409 by {@link CheckUniqueIdentity}) but that shares an existing
 * owner's {@code lastName} and {@code postcode} while having a <em>different</em> telephone.
 *
 * <p>When such an existing owner is found the new owner is still created, with
 * {@code possibleDuplicate} set to {@code true} and {@code possibleDuplicateOf} set to the matching
 * owner's id (the earliest such owner when several match); otherwise {@code possibleDuplicate} is
 * {@code false} and {@code possibleDuplicateOf} is left unset.
 *
 * <p>Runs after {@link CheckUniqueIdentity} (so hard duplicates are already out) and before
 * {@link SaveOwner} (so the new owner is not compared against itself).
 */
public class CheckPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String postcode = normalize(owner.getPostcode());
        String telephone = normalizeTelephone(owner.getTelephone());

        Integer matchId = null;
        if (!lastName.isEmpty() && !postcode.isEmpty()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue; // never match self
                }
                if (lastName.equals(normalize(existing.getLastName()))
                        && postcode.equals(normalize(existing.getPostcode()))
                        && !telephone.equals(normalizeTelephone(existing.getTelephone()))) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }

        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeTelephone(String telephone) {
        try {
            return OwnerTelephone.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            // A value that cannot form a valid E.164 number is represented by its raw form
            // (or empty when absent) so the comparison stays total.
            return telephone == null ? "" : telephone.trim();
        }
    }
}
