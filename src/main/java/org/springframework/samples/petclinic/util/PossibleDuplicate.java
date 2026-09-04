package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft duplicate detection. A new owner that is not a hard duplicate but whose
 * {@code soundex(lastName)} and postcode match an existing owner (so only their differing
 * identity keys keep them apart) is a <em>possible</em> duplicate; the matching owner's id is
 * recorded on the candidate.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /** Record on {@code candidate} whether it possibly duplicates an existing owner (matching
     *  {@code soundex(lastName)} and postcode but a different identity key) and, if so, that
     *  owner's id. A declared household member ({@code sharesHousehold}) is never a suspected
     *  duplicate. */
    public static void assign(Owner candidate, boolean sharesHousehold, Collection<Owner> existing) {
        Integer match = sharesHousehold ? null : matchId(candidate, existing);
        candidate.setPossibleDuplicate(match != null);
        candidate.setPossibleDuplicateOf(match);
    }

    private static Integer matchId(Owner candidate, Collection<Owner> existing) {
        String key = IdentityKey.of(candidate);
        String soundex = Soundex.of(candidate.getLastName());
        for (Owner other : existing) {
            if (candidate.getPostcode() != null
                && candidate.getPostcode().equals(other.getPostcode())
                && soundex.equals(Soundex.of(other.getLastName()))
                && !key.equals(IdentityKey.of(other))) {
                return other.getId();
            }
        }
        return null;
    }
}
