package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft duplicate detection on create. Household members are no longer rejected on create — a new
 * owner sharing an existing owner's household is admitted (its membership level is capped by
 * {@link CapMembershipLevel}) rather than blocked — so this step only raises a non-rejecting signal.
 *
 * <p>This step flags a weaker signal: a new owner whose {@code identityKey} <em>differs</em> from
 * an existing owner's while they share a last-name {@link Soundex} code and
 * the same {@code postcode}. In that case the owner is still created, but {@code possibleDuplicate}
 * is set true and {@code possibleDuplicateOf} is set to the matching owner's id (the lowest-id
 * match when several exist). Otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left absent.
 *
 * <p>A matching identityKey is a <em>hard</em> duplicate already rejected upstream by
 * {@link RejectDuplicateIdentity}, so it never reaches here; two owners sharing a last name and
 * postcode but carrying different telephones have different identity keys and so are a soft match
 * rather than a rejection.
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

        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(sharesHousehold)) {
            // Declared household member: expected to share a household, not a suspected duplicate.
            return;
        }

        if (lastName == null || postcode == null) {
            return;
        }

        String soundex = Soundex.of(lastName);
        String identityKey = OwnerIdentity.identityKey(owner);

        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                // A soft-deleted owner is not a duplicate signal.
                continue;
            }
            if (!soundex.equals(Soundex.of(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            // A matching identityKey is a hard duplicate (rejected upstream); the soft signal is
            // for a different key that still shares the last-name soundex and postcode.
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
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
}
