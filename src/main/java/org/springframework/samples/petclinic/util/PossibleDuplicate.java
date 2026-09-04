package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft duplicate detection. A new owner that is not a hard duplicate but shares an existing
 * owner's last name and postcode while having a different telephone is a <em>possible</em>
 * duplicate; the matching owner's id is recorded on the candidate.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /** Record on {@code candidate} whether it possibly duplicates an existing owner (same last
     *  name and postcode, different telephone) and, if so, that owner's id. */
    public static void assign(Owner candidate, Collection<Owner> existing) {
        Integer match = matchId(candidate, existing);
        candidate.setPossibleDuplicate(match != null);
        candidate.setPossibleDuplicateOf(match);
    }

    private static Integer matchId(Owner candidate, Collection<Owner> existing) {
        for (Owner other : existing) {
            if (candidate.getPostcode() != null
                && candidate.getPostcode().equals(other.getPostcode())
                && candidate.getLastName().equalsIgnoreCase(other.getLastName())
                && !digits(candidate.getTelephone()).equals(digits(other.getTelephone()))) {
                return other.getId();
            }
        }
        return null;
    }

    private static String digits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
