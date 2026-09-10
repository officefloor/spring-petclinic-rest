package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.Soundex;

/**
 * Flags an owner as a soft-match ("possible duplicate") of an existing owner: one that is
 * <em>not</em> a hard duplicate (its {@link OwnerIdentityKey} differs, see
 * {@link EnsureUniqueOwnerIdentity}) yet shares another owner's {@link Soundex} last name and exact
 * postcode. Because the telephone is part of the identity key, two owners with the same (Soundex)
 * last name and postcode but different telephones have different keys — so the second is created and
 * flagged here rather than rejected.
 *
 * <p>Such an owner is still created; this step only records the transient {@code possibleDuplicate}
 * flag and, when true, the {@code possibleDuplicateOf} id of the matching owner — the earliest
 * (lowest id) match when several exist. When nothing matches, {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is null. Soft-deleted owners are ignored.
 *
 * <p>Runs on both the create pipeline (after Save) and the read pipeline, so the flag is identical
 * whether returned from the create response or a later GET, mirroring
 * {@link AssignOwnerBulkSignupWarning} and {@link AssignOwnerHouseholdSize}. Both derived values
 * are never persisted.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String identityKey = OwnerIdentityKey.of(owner);
        String soundex = Soundex.of(owner.getLastName());
        Owner match = ownerRepository.findAll().stream()
                .filter(existing -> existing.getId() != null
                        && (owner.getId() == null || !existing.getId().equals(owner.getId())))
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> !identityKey.equals(OwnerIdentityKey.of(existing)))
                .filter(existing -> postcode.equals(existing.getPostcode())
                        && soundex.equals(Soundex.of(existing.getLastName())))
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
}
