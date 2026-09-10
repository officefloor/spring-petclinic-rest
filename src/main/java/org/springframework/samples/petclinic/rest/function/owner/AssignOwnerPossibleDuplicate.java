package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags an owner as a soft-match ("possible duplicate") of an existing owner: one that is
 * <em>not</em> a hard duplicate (see {@link EnsureUniqueOwnerIdentity}) yet shares another
 * owner's {@code lastName} (compared case-insensitively) and {@code postcode} while carrying a
 * different telephone (compared in canonical E.164 form, see {@link OwnerTelephone}).
 *
 * <p>A fellow member of the same household (same deterministic {@code householdId}, see
 * {@link OwnerHousehold}) is excluded: sharing a household is a <em>declared</em> relationship
 * admitted via the {@code sharesHousehold} flag, not a suspected duplicate. Because the
 * householdId is derived from (lastName, postcode), this is exactly the set of owners a soft
 * match would otherwise catch, so a declared household member is never flagged.
 *
 * <p>Such an owner is still created; this step only records the transient {@code possibleDuplicate}
 * flag and, when true, the {@code possibleDuplicateOf} id of the matching owner — the earliest
 * (lowest id) match when several exist. When nothing matches, {@code possibleDuplicate} is false
 * and {@code possibleDuplicateOf} is null.
 *
 * <p>Runs on both the create pipeline (after Save) and the read pipeline, so the flag is identical
 * whether returned from the create response or a later GET, mirroring
 * {@link AssignOwnerBulkSignupWarning} and {@link AssignOwnerHouseholdSize}. Both derived values
 * are never persisted.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String telephone = OwnerTelephone.canonical(owner.getTelephone());
        String householdId = owner.getHouseholdId();
        Owner match = ownerRepository.findAll().stream()
                .filter(existing -> existing.getId() != null
                        && (owner.getId() == null || !existing.getId().equals(owner.getId())))
                .filter(existing -> householdId == null || !householdId.equals(existing.getHouseholdId()))
                .filter(existing -> equalsIgnoreCase(existing.getLastName(), lastName)
                        && postcode.equals(existing.getPostcode())
                        && !telephone.equals(OwnerTelephone.canonical(existing.getTelephone())))
                .min(Comparator.comparingInt(Owner::getId))
                .orElse(null);
        if (match == null) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
        else {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
