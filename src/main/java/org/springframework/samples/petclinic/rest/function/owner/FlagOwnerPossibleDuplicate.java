package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Stamps the soft-duplicate flags onto the newly built owner. A create that is not a hard
 * duplicate (already enforced by {@link CheckOwnerIdentityUnique}) is still flagged as a
 * possible duplicate when it shares an existing owner's last name (compared case-insensitively)
 * and postcode while carrying a different telephone. When such a match is found,
 * {@code possibleDuplicate} is set {@code true} and {@code possibleDuplicateOf} to the matching
 * owner's id (the earliest-created match when several exist); otherwise {@code possibleDuplicate}
 * is {@code false} and {@code possibleDuplicateOf} is left absent.
 *
 * <p>Runs before {@link SaveOwner}, so the new owner is not yet persisted and cannot match itself.
 */
public class FlagOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode to share — cannot be a possible duplicate
        }
        String lastName = canonical(owner.getLastName());
        String telephone = owner.getTelephone();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (postcode.equals(existing.getPostcode())
                    && lastName.equals(canonical(existing.getLastName()))
                    && !equalsTelephone(telephone, existing.getTelephone())) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static boolean equalsTelephone(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String canonical(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
