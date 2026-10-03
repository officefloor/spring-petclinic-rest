package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft duplicate detection on create. Household members are no longer rejected on create — a new
 * owner sharing an existing owner's household is admitted (its membership level is capped by
 * {@link CapMembershipLevel}) rather than blocked — so this step only raises a non-rejecting signal.
 *
 * <p>This step flags a weaker signal: a new owner that shares an existing owner's
 * {@code lastName} (case-insensitive) and
 * {@code postcode} while carrying a <em>different</em> telephone. In that case the owner is
 * still created, but {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} is
 * set to the matching owner's id (the lowest-id match when several exist). Otherwise
 * {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left absent.
 *
 * <p>A declared household member ({@code sharesHousehold} true) is never flagged: sharing an
 * existing owner's last name and postcode is expected for it rather than a suspected duplicate.
 *
 * <p>Runs after the telephone and postcode have been normalized/assigned and before
 * {@link SaveOwner}, so the flags are fixed at creation time and returned on later reads.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = normalizedTelephone(owner);

        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(sharesHousehold)) {
            // Declared household member: expected to share a household, not a suspected duplicate.
            return;
        }

        if (lastName == null || postcode == null) {
            return;
        }

        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                // A soft-deleted owner is not a duplicate signal.
                continue;
            }
            if (!lastName.equalsIgnoreCase(existing.getLastName())) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            // A different telephone is what distinguishes a soft match from a hard
            // (identityKey) duplicate, which has already been rejected upstream. Compare
            // through the same normalization as the hard check so the two can't drift.
            if (telephone.equals(normalizedTelephone(existing))) {
                continue;
            }
            if (matchId == null || existing.getId() < matchId) {
                matchId = existing.getId();
            }
        }

        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }

    /**
     * The owner's telephone in the same normalized form {@link OwnerIdentity} compares on:
     * the E.164 value via {@link OwnerTelephones#toE164}, falling back to the stored raw value
     * (or empty) when it cannot be normalized, so a stored value that predates the pipeline
     * still compares correctly.
     */
    private static String normalizedTelephone(Owner owner) {
        String telephone = OwnerTelephones.toE164(owner.getTelephone());
        if (telephone == null) {
            telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        return telephone;
    }
}
